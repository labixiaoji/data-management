import React, { useRef, useEffect } from 'react'
import {Avatar, Card} from 'antd'
import {SmileOutlined, UserOutlined} from '@ant-design/icons'

const MessageList = ({ messages }) => {
    const messageEndRef = useRef(null)

    const scrollToBottom = () => {
        messageEndRef.current.scrollIntoView({ behavior: 'smooth' })
    }

    useEffect(() => {
        scrollToBottom()
    }, [messages])

    return (
        <div style={{ overflowY: 'auto', maxHeight: '550px' }}>
            {messages.map((item, index) => (
                <div key={index} style={{ display: 'flex', alignItems: 'flex-start' }}>
                    {item.type === 'robot' ? (
                        <Avatar
                            style={{
                                backgroundColor: '#fde3cf',
                                color: '#f56a00',
                            }}
                            icon={<SmileOutlined />}
                        />
                    ) : (
                        <div style={{ flex: 1 }} />
                    )}
                    <Card
                        style={{
                            maxWidth: '70%',
                            wordWrap: 'break-word',
                            padding: '10px',
                            backgroundColor: item.type === 'robot' ? '#fde3cf' : '#87d068',
                        }}
                    >
                        {item.content}
                    </Card>
                    {item.type === 'user' ? (
                        <Avatar
                            style={{
                                backgroundColor: '#87d068',
                                marginLeft: '8px',
                            }}
                            icon={<UserOutlined />}
                        />
                    ) : (
                        <div style={{ flex: 1 }} />
                    )}
                </div>
            ))}
            <div ref={messageEndRef} />
        </div>
    )
}

export default MessageList
