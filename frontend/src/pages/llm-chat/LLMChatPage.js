import React from 'react'
import {Row, Col, Card} from 'antd'
import ChatBox from './components/ChatBox'
import CodeExecution from './components/CodeExecution'

const LLMChatPage = () => {

    const [llmRes, setLlmRes] = React.useState("");
    const [llmLog, setLlmLog] = React.useState("");
    const [llmCode, setLlmCode] = React.useState("");
    const [llmCodeRes, setLlmCodeRes] = React.useState("");

    const handleLlmCode = (code) => {
        setLlmCode(code);
    }
    const handleLlmCodeRes = (codeRes) => {
        setLlmCodeRes(codeRes);
    }

    const handleLlmLog = (log) => {
        console.log(log);
        setLlmLog(log);
    }

    return (
        <Row>
            <Col span={14}  >
                <Card title='会话窗口' bordered={true} style={{marginRight:'15px'}}>
                    <ChatBox
                        onCodeReceived={handleLlmCode}
                        onExecuteResultReceived={handleLlmCodeRes}
                        onLlmLogReceived={handleLlmLog}
                    />
                </Card>
            </Col>
            <Col span={10}>
                <Card title='代码执行区'>
                    <CodeExecution
                        code={llmCode}
                        codeRes={llmCodeRes}
                    />
                </Card>
            </Col>
            <Row>
                <h1></h1>
            </Row>
        </Row>

    )
}

export default LLMChatPage
