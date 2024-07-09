import pandas as pd
from sklearn.cluster import KMeans
from mlxtend.frequent_patterns import apriori, association_rules
import networkx as nx
import matplotlib.pyplot as plt

# 读取Excel文件
file_path = 'F:\Desktop\Olist\import\mining1.xlsx'
df = pd.read_excel(file_path)

# 查看数据基本信息
print("Data Columns:\n", df.columns)
print("\nData Overview:\n", df.head())

# 确认实际列名后调整以下代码
order_id_col = 'order_id'
customer_id_col = 'customer_id'
product_id_col = 'product_id'
type_col = 'type'  # 促销活动可以从type中提取

# -------------------------------
# 1. 数据预处理
# -------------------------------
# 创建一个模拟的购买金额列，假设每个产品价格为100（仅供演示）
df['purchase_amount'] = 100

# 提取用于聚类的特征
features = df[['purchase_amount']]

# -------------------------------
# 2. 聚类分析
# -------------------------------
# 使用K-Means进行聚类
kmeans = KMeans(n_clusters=3, random_state=0)
df['cluster'] = kmeans.fit_predict(features)

# 打印聚类结果
print("Cluster Results:\n", df)

# -------------------------------
# 3. 关联分析
# -------------------------------
# 转换为购物篮格式
basket = df.pivot_table(index=customer_id_col, columns=product_id_col, aggfunc=lambda x: 1, fill_value=0)

# 计算频繁项集
frequent_itemsets = apriori(basket, min_support=0.1, use_colnames=True)

# 生成关联规则
rules = association_rules(frequent_itemsets, metric="lift", min_threshold=1.0)

# 打印关联规则
print("Association Rules:\n", rules)

# -------------------------------
# 4. 结果可视化
# -------------------------------

# 可视化聚类结果
plt.scatter(df[order_id_col], df['cluster'], c=df['cluster'], cmap='viridis')
plt.xlabel('Order ID')
plt.ylabel('Cluster')
plt.title('Customer Purchase Clustering')
plt.show()

# 可视化关联规则
G = nx.DiGraph()

# 添加节点和边
for _, row in rules.iterrows():
    for antecedent in row['antecedents']:
        for consequent in row['consequents']:
            G.add_edge(antecedent, consequent, weight=row['lift'])

# 画图
pos = nx.spring_layout(G)
nx.draw(G, pos, with_labels=True, node_size=3000, node_color='skyblue', font_size=10, font_weight='bold', edge_color='gray')
plt.title('Association Rules Network')
plt.show()
