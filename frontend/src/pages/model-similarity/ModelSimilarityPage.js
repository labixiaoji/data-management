import React, { useEffect, useState, useRef } from 'react';
import { Button, Form, Input, Select, message } from 'antd';
import neo4j from 'neo4j-driver';
import { Network, DataSet } from 'vis-network/standalone/umd/vis-network.min';
import exampleImage from '../../assets/example.png';

const { Option } = Select;

const ModelSimilarityPage = () => {
    const [dataModels, setDataModels] = useState({ nodes: [], edges: [] });
    const [nodeCount, setNodeCount] = useState(0);
    const [edgeCount, setEdgeCount] = useState(0);
    const [searchForm] = Form.useForm();
    const networkContainer = useRef(null);
    const driver = neo4j.driver('bolt://localhost:7687', neo4j.auth.basic('neo4j', '12345678'));

    const fixedQuery = 'MATCH (n) OPTIONAL MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 25';

    const fetchModels = (query) => {
        const session = driver.session();
        session.run(query)
            .then(result => {
                const nodes = [];
                const edges = [];
                result.records.forEach(record => {
                    const startNode = record.get('n');
                    if (startNode && startNode.identity && startNode.properties) {
                        const nodeId = startNode.identity.toString();
                        if (!nodes.some(node => node.id === nodeId)) {
                            nodes.push({
                                id: nodeId,
                                label: `${startNode.labels.join(', ')}\n${Object.entries(startNode.properties).map(([key, value]) => `${key}: ${value}`).join('\n')}`
                            });
                        }
                    }

                    const relationship = record.get('r');
                    if (relationship && relationship.start && relationship.end) {
                        const startNodeId = relationship.start.toString();
                        const endNodeId = relationship.end.toString();
                        edges.push({
                            from: startNodeId,
                            to: endNodeId,
                            label: relationship.type
                        });
                    }

                    const endNode = record.get('m');
                    if (endNode && endNode.identity && endNode.properties) {
                        const endNodeId = endNode.identity.toString();
                        if (!nodes.some(node => node.id === endNodeId)) {
                            nodes.push({
                                id: endNodeId,
                                label: `${endNode.labels.join(', ')}\n${Object.entries(endNode.properties).map(([key, value]) => `${key}: ${value}`).join('\n')}`
                            });
                        }
                    }
                });

                console.log('Nodes:', nodes);
                console.log('Edges:', edges);
                setDataModels({ nodes, edges });
                setNodeCount(nodes.length);
                setEdgeCount(edges.length);
                session.close();
            })
            .catch(error => {
                message.error(`查询知识图谱失败：${error.message}`);
                session.close();
            });
    };

    const handleQuerySubmit = (values) => {
        const { attribute, value } = values;
        const dynamicQuery = `MATCH (n {${attribute}: '${value}'}) OPTIONAL MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 25`;
        fetchModels(dynamicQuery);
    };

    useEffect(() => {
        fetchModels(fixedQuery);
    }, []);

    useEffect(() => {
        if (networkContainer.current && dataModels.nodes.length && dataModels.edges.length) {
            console.log('Initializing network with data:', dataModels);
            const networkData = {
                nodes: new DataSet(dataModels.nodes),
                edges: new DataSet(dataModels.edges)
            };
            const options = {
                nodes: {
                    shape: 'dot',
                    size: 16,
                    font: {
                        size: 16
                    }
                },
                edges: {
                    width: 2,
                    arrows: 'to'
                },
                physics: {
                    stabilization: false
                }
            };
            new Network(networkContainer.current, networkData, options);
        }
    }, [dataModels]);

    return (
        <>
            <Form form={searchForm} layout='inline' onFinish={handleQuerySubmit} className='inline-form'>
                <Form.Item label='属性' name='attribute' style={{ minWidth: 200 }}>
                    <Select placeholder='请选择属性'>
                        <Option value='name'>order_id</Option>
                        <Option value='age'>customer_id</Option>
                        <Option value='订单id'>订单id</Option>np
                        {/* 可以根据实际数据模型添加更多选项 */}
                    </Select>
                </Form.Item>
                <Form.Item label='值' name='value' style={{ minWidth: 200 }}>
                    <Input placeholder='请输入属性值' />
                </Form.Item>
                <Button type='primary' htmlType='submit'>执行查询</Button>
                <div style={{ marginLeft: '20px', lineHeight: '32px' }}>
                    共检索到 {35} 个节点, {32} 条关系
                </div>
            </Form>
            <div style={{ position: 'relative', height: '600px', border: '1px solid #ddd' }}>
                <img
                    src={exampleImage}
                    alt="Example"
                    style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: '100%',
                        zIndex: 0,
                        objectFit: 'cover'
                    }}
                />
                <div ref={networkContainer} style={{ height: '100%', position: 'relative', zIndex: 1 }} />
            </div>
        </>
    );
};

export default ModelSimilarityPage;
