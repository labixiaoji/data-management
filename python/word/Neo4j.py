import pandas as pd
from neo4j import GraphDatabase

uri = "bolt://localhost:7687"
user = "neo4j"
password = "12345678"


class Neo4jConnection:
    def __init__(self, uri, user, password):
        self._driver = GraphDatabase.driver(uri, auth=(user, password))

    def close(self):
        self._driver.close()

    def run_query(self, query, parameters=None):
        with self._driver.session() as session:
            result = session.run(query, parameters)
            return result


def create_table_node(conn, node_type, key_attribute):
    # 创建Table节点并添加key attribute
    conn.run_query(f"MERGE (t:Table {{name: '{node_type}', key_attribute: '{key_attribute}'}})")


def connect_tables_by_attributes(conn):
    query = """
    MATCH (t1:Table)-[:HAS_ATTRIBUTE]->(a:Attribute)<-[:HAS_ATTRIBUTE]-(t2:Table)
    WHERE t1 <> t2
    MERGE (t1)-[:CONNECTED_BY {attribute: a.name}]->(t2)
    """
    conn.run_query(query)


def import_data(conn, file_path, node_type, key_attribute):
    df = pd.read_csv(file_path)
    create_table_node(conn, node_type, key_attribute)

    for index, row in df.iterrows():
        # 创建小的Order实体节点
        entity_properties = ', '.join([f"{key}: '{row[key]}'" for key in row.keys() if pd.notna(row[key])])
        entity_query = f"""
        CREATE (e:{node_type}Entity {{{entity_properties}}})
        WITH e
        MATCH (t:Table {{name: '{node_type}'}})
        MERGE (t)-[:CONTAINS]->(e)
        """
        conn.run_query(entity_query)

        # 为每个属性创建Data节点，并与实体节点连接
        for key in row.keys():
            if pd.notna(row[key]):
                data_query = f"""
                MATCH (e:{node_type}Entity {{{', '.join([f"{k}: '{v}'" for k, v in row.items() if pd.notna(v)])}}})
                MERGE (d:Data {{value: '{row[key]}', attribute: '{key}'}})
                WITH e, d
                CREATE (e)-[:{key.upper()}]->(d)
                """
                conn.run_query(data_query)

#
# def merge_entities_by_key_attribute(conn, node_type, key_attribute):
#     query = f"""
#     MATCH (t:Table {{name: '{node_type}'}})-[:CONTAINS]->(e1:{node_type}Entity),
#           (t)-[:CONTAINS]->(e2:{node_type}Entity)
#     WHERE e1.{key_attribute} = e2.{key_attribute} AND id(e1) <> id(e2)
#     CALL apoc.refactor.mergeNodes([e1, e2]) YIELD node
#     RETURN node
#     """
#     conn.run_query(query)


# 建立连接
conn = Neo4jConnection(uri, user, password)

# 导入数据并指定key attribute
import_data(conn, "F:/Desktop/Olist/import/acustomers.csv", 'Customer', 'customer_id')
import_data(conn, "F:/Desktop/Olist/import/ageolocation.csv", 'Geolocation', 'geolocation_id')
import_data(conn, "F:/Desktop/Olist/import/aorderitems.csv", 'OrderItem', 'order_item_id')
import_data(conn, "F:/Desktop/Olist/import/aorderpayments.csv", 'OrderPayment', 'payment_id')
import_data(conn, "F:/Desktop/Olist/import/aorderreviews.csv", 'OrderReview', 'review_id')
import_data(conn, "F:/Desktop/Olist/import/aorders.csv", 'Order', 'order_id')
import_data(conn, "F:/Desktop/Olist/import/aproducts.csv", 'Product', 'product_id')
import_data(conn, "F:/Desktop/Olist/import/asellers.csv", 'Seller', 'seller_id')

# 数据补全
# merge_entities_by_key_attribute(conn, 'Customer', 'customer_id')
# merge_entities_by_key_attribute(conn, 'Geolocation', 'geolocation_id')
# merge_entities_by_key_attribute(conn, 'OrderItem', 'order_item_id')
# merge_entities_by_key_attribute(conn, 'OrderPayment', 'payment_id')
# merge_entities_by_key_attribute(conn, 'OrderReview', 'review_id')
# merge_entities_by_key_attribute(conn, 'Order', 'order_id')
# merge_entities_by_key_attribute(conn, 'Product', 'product_id')
# merge_entities_by_key_attribute(conn, 'Seller', 'seller_id')

# 关闭连接
conn.close()
