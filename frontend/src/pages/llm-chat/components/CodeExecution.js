import React, { useState, useEffect } from 'react'
import {Input, Button, message} from 'antd'

const { TextArea } = Input

const CodeExecution = (props) => {
    const [code, setCode] = useState('')
    const [result, setResult] = useState('')

    // 使用 useEffect 钩子监听 props.codeRes 的变化
    useEffect(() => {
        console.log(props.code)
        if (props.code) {
            setCode(props.code);
        }
    }, [props.code]);

    const executeCode = () => {
        // 在这里将代码发送到后端执行，并获取结果
        // 假设这里是发送请求的函数 fetchCodeExecutionResult

        // 示例：假设后端接收到 code 并返回结果 result
        // 这里使用了 setTimeout 模拟异步请求
        message.loading({ content: '执行中...', key: 'execution' })
        setTimeout(() => {
            // 假设这里是从后端获取的结果
            const mockResult = props.codeRes
            setResult(mockResult)
            message.success({ content: '执行完成', key: 'execution', duration: 3 })
        }, 1500)
    }

    const handleCodeChange = (e) => {
        setCode(e.target.value)
    }

    return (
        <div>
            <TextArea
                placeholder='在此粘贴代码'
                value={code}
                onChange={handleCodeChange}
                //固定大小
                style={{ width: '550px', height: '150px' }}
            />
            <Button type='primary' onClick={executeCode} style={{ marginTop: '10px' }}>
                执行代码
            </Button>
            <div style={{ marginTop: '20px' }}>
                <h3>执行结果：</h3>
                <pre style={{ whiteSpace: 'pre-wrap', backgroundColor: '#f5f5f5', padding: '10px', overflowY: 'auto', height: '407px' }}>
          {result}
        </pre>
            </div>
        </div>
    )
}

export default CodeExecution
