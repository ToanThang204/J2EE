// Candidate Chat JavaScript

let currentConversationId = null;

document.addEventListener('DOMContentLoaded', function() {
    initChat();
    loadConversations();
});

function initChat() {
    const chatToggle = document.querySelector('.chat-toggle-btn');
    const chatWidget = document.getElementById('chatWidget');
    
    if (chatToggle) {
        chatToggle.addEventListener('click', toggleChat);
    }
    
    const chatForm = document.getElementById('chatForm');
    if (chatForm) {
        chatForm.addEventListener('submit', sendMessage);
    }
}

function toggleChat() {
    const widget = document.getElementById('chatWidget');
    if (widget) {
        widget.style.display = widget.style.display === 'none' ? 'block' : 'none';
        if (widget.style.display === 'block') {
            loadMessages();
        }
    }
}

async function loadConversations() {
    try {
        const response = await api.get('/chat/conversations');
        
        if (response && response.success && response.data) {
            displayConversations(response.data);
        }
    } catch (error) {
        console.error('Error loading conversations:', error);
    }
}

function displayConversations(conversations) {
    // TODO: Implement conversation list display
}

async function loadMessages() {
    if (!currentConversationId) return;
    
    try {
        const response = await api.get(`/chat/conversations/${currentConversationId}/messages`);
        
        if (response && response.success && response.data) {
            displayMessages(response.data);
        }
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

function displayMessages(messages) {
    const container = document.getElementById('chatMessages');
    if (!container) return;
    
    container.innerHTML = messages.map(msg => `
        <div class="chat-message ${msg.senderId === getCurrentUserId() ? 'sent' : 'received'}">
            <div class="chat-message-bubble">${msg.content || ''}</div>
            <span class="chat-message-time">${formatDate(msg.createdAt)}</span>
        </div>
    `).join('');
    
    container.scrollTop = container.scrollHeight;
}

async function sendMessage(e) {
    e.preventDefault();
    
    if (!currentConversationId) {
        showToast('Vui lòng chọn cuộc trò chuyện', 'error');
        return;
    }
    
    const messageInput = document.getElementById('messageInput');
    const message = messageInput ? messageInput.value.trim() : '';
    
    if (!message) return;
    
    try {
        const response = await api.post('/chat/messages', {
            conversationId: currentConversationId,
            content: message
        });
        
        if (response && response.success) {
            messageInput.value = '';
            loadMessages();
        }
    } catch (error) {
        showToast('Lỗi khi gửi tin nhắn', 'error');
    }
}

function getCurrentUserId() {
    const user = localStorage.getItem('user');
    if (user) {
        try {
            return JSON.parse(user).id;
        } catch (e) {
            return null;
        }
    }
    return null;
}

// Auto refresh messages every 3 seconds when chat is open
setInterval(() => {
    const widget = document.getElementById('chatWidget');
    if (widget && widget.style.display !== 'none' && currentConversationId) {
        loadMessages();
    }
}, 3000);

