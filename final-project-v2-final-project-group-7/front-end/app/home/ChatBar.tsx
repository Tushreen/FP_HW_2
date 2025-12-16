'use client';

import React from 'react';

interface Props {
  currentUser?: string;
  // optional username to open when ChatBar mounts or changes
  targetUser?: string;
}

interface MessageDto {
  fromId?: string;
  toId: string;
  message?: string;
  conversationId?: string;
  uniqueId?: string;
  // ui helpers
  from?: string;
  to?: string;
  text?: string;
  time?: string;
}

export default function ChatBar({ currentUser, targetUser }: Props) {
  const [messages, setMessages] = React.useState<MessageDto[]>([]);
  // recipient input removed; use targetUserInput state instead
  const [text, setText] = React.useState('');
  const messagesRef = React.useRef<HTMLDivElement | null>(null);
  if (!currentUser || !targetUser) return;

  const ids = [currentUser, targetUser].slice().sort();
  const conversationId = `${ids[0]}_${ids[1]}`;

  React.useEffect(() => {
    if (messagesRef.current) {
      messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
    }
  }, [messages]);

  async function loadConversation() {
    try {
      const res = await fetch(`/api/getConversation?conversationId=${encodeURIComponent(conversationId)}`);
      if (!res.ok) {
        console.error('Failed to load conversation', await res.text());
        return;
      }
      const data = await res.json();
      console.log('Loaded messages:', data.data);
      setMessages(data.data || []);
    } catch (err) {
      console.error('Error loading conversation', err);
    }
  }


  // load conversation when currentUser or conversationId changes
  React.useEffect(() => {
    loadConversation();
  }, [currentUser, conversationId]);

  async function send() {

    // compute conversation id for this pair
    const ids = [currentUser || '', targetUser || ''].slice().sort();
    const conversationId = `${ids[0]}_${ids[1]}`;

    const payload: MessageDto = {
      toId: targetUser || '',
      message: text.trim(),
      conversationId,
      fromId: currentUser,
    };

    // optimistic UI message while waiting for server
    const optimistic = {
      from: currentUser || 'me',
      to: targetUser || '',
      text: text.trim(),
      time: new Date().toLocaleTimeString(),
    } as MessageDto;

    loadConversation();
    setText('');

    try {
      const res = await fetch('/api/sendMessage', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        // map server response to UI-friendly shape
        const serverMsg: MessageDto = {
          from: data.from || data.fromId || currentUser || 'me',
          to: data.to || data.toId || targetUser || '',
          text: data.text || data.message || payload.message,
          time: data.time || data.createdAt || new Date().toLocaleTimeString(),
          // keep any ids returned
          fromId: data.fromId || payload.fromId,
          toId: data.toId || payload.toId,
          uniqueId: data.uniqueId || data.id,
        };

        // replace the optimistic message with server message
        setMessages((prev) => {
          const copy = [...prev];
          // find last optimistic message matching text and recipient
          const idx = copy.findLastIndex((m) => (m.text || m.message) === optimistic.text && (m.to || m.toId) === optimistic.to);
          if (idx !== -1) {
            copy[idx] = serverMsg;
          } else {
            copy.push(serverMsg);
          }
          return copy;
        });
      } else {
        console.error('Failed to send message', await res.text());
      }
    } catch (err) {
      console.error('Network error sending message', err);
    }
  }

  return (
    <div style={{ marginTop: 24, borderTop: '1px solid #eee', paddingTop: 16 }}>
      <h2 style={{ margin: '0 0 8px 0' }}>Chat</h2>

      <div
        ref={messagesRef}
        style={{
          height: 160,
          overflowY: 'auto',
          padding: 8,
          border: '1px solid #e6e6e6',
          borderRadius: 6,
          background: '#fafafa',
        }}
      >
        {messages.length === 0 ? (
          <div style={{ color: '#000' }}>No messages yet.</div>
        ) : (
          messages.map((m, i) => (
            <div key={m.uniqueId || i} style={{ marginBottom: 8, display: 'flex', alignItems: 'flex-start', gap: 8 }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: '#333' }}>
                  {(m.time || '')} • {(m.from || m.fromId)} → {(m.to || m.toId)}
                </div>
                <div style={{ color: '#000' }}>{m.text || m.message}</div>
              </div>
              {(m.fromId === currentUser || m.from === currentUser) && m.uniqueId && (
                <button
                  onClick={async () => {
                    try {
                      const res = await fetch('/api/deleteMessage', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ messageId: m.uniqueId }),
                      });
                      const data = await res.json();
                      if (data.status) {
                        loadConversation();
                      } else {
                        alert(data.message || 'Failed to delete message');
                      }
                    } catch (err) {
                      console.error('Error deleting message', err);
                    }
                  }}
                  style={{ padding: '2px 8px', fontSize: 12, background: '#ff6b6b', color: 'white', border: 'none', borderRadius: 3, cursor: 'pointer' }}
                >
                  Delete
                </button>
              )}
            </div>
          ))
        )}
      </div>

      <div style={{ marginTop: 8 }}>
        <textarea
          placeholder="Type a message"
          value={text}
          onChange={(e) => setText(e.target.value)}
          style={{ width: '100%', padding: 8, minHeight: 72, resize: 'vertical' }}
        />
      </div>

      <div style={{ display: 'flex', gap: 8, marginTop: 8, alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: 8 }}>
          <button
            onClick={send}
            style={{ height: 44, padding: '0 16px', alignSelf: 'center' }}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}
