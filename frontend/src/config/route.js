import {
    DataModelPage,
    DataModelDetailPage,
    DataSourcePage,
    DataAssetPage,
    LLMChatPage,
    DataMapPage,
    ModelSimilarityPage
} from '../pages'
import {
    CloudOutlined,
    DatabaseFilled,
    DotChartOutlined,
    FileSearchOutlined,
    MergeCellsOutlined,
    MoneyCollectOutlined,
    RobotOutlined, ShareAltOutlined
} from '@ant-design/icons'

const routes = [
    {
        path: '/data-aggregation',
        label: '数据汇聚',
        meta: {
            icon: <MergeCellsOutlined/>
        },
        children: [
            {
                path: '/model',
                component: <DataModelPage/>,
                label: '数据模型管理',
                meta: {
                    icon: <DatabaseFilled/>
                }
            },
            {
                path: '/model/:modelId',
                component: <DataModelDetailPage/>,
                label: '数据模型详情',
                meta: {
                    hidden: true
                }
            },
            {
                path: '/source',
                component: <DataSourcePage/>,
                label: '数据源管理',
                meta: {
                    icon: <DotChartOutlined/>
                }
            }
        ],
    },
    {
        path: '/data-service',
        label: '数据服务',
        meta: {
            icon: <CloudOutlined/>
        },
        children: [
            {
                path: '/asset',
                component: <DataAssetPage/>,
                label: '数据资产',
                meta: {
                    icon: <MoneyCollectOutlined/>
                }
            },
            {
                path: '/map',
                component: <DataMapPage/>,
                label: '数据地图',
                meta: {
                    icon: <ShareAltOutlined/>
                }
            },
            {
                path: '/knowledgegraph',
                component: <ModelSimilarityPage/>,
                label: '知识图谱',
                meta: {
                    icon: <FileSearchOutlined/>
                }
            },
            // {
            //     path: '/chat',
            //     component: <LLMChatPage/>,
            //     label: '大模型交互',
            //     meta: {
            //         icon: <RobotOutlined/>
            //     }
            // }
        ]
    }
]

export default routes
