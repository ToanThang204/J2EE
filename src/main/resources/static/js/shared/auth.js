// Authentication JavaScript

// Handle login form
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', handleForgotPassword);
    }
    
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', handleResetPassword);
    }
});

async function handleLogin(e) {
    e.preventDefault();
    
    const form = e.target;
    const email = form.email.value;
    const password = form.password.value;
    
    clearErrors();
    
    if (!validateEmail(email)) {
        showError('emailError', 'Email không hợp lệ');
        return;
    }
    
    if (!password) {
        showError('passwordError', 'Vui lòng nhập mật khẩu');
        return;
    }
    
    try {
        const response = await api.post('/auth/login', { email, password });
        
        if (response && response.success) {
            if (response.data && response.data.token) {
                localStorage.setItem('token', response.data.token);
                console.log('Auth: Token saved to localStorage');
                if (response.data.user) {
                    localStorage.setItem('user', JSON.stringify(response.data.user));
                    console.log('Auth: User saved to localStorage:', response.data.user);
                }
            }
            
            showToast('Đăng nhập thành công!', 'success');
            
            // Redirect to homepage
            setTimeout(() => {
                sessionStorage.clear();
                console.log('Auth: Redirecting to homepage...');
                window.location.replace('/');
            }, 500);
        }
    } catch (error) {
        showError('emailError', error.message || 'Đăng nhập thất bại');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const form = e.target;
    const name = form.name.value;
    const email = form.email.value;
    const password = form.password.value;
    const passwordConfirmation = form.passwordConfirmation.value;
    
    clearErrors();
    
    if (!name || name.trim().length < 2) {
        showError('nameError', 'Tên phải có ít nhất 2 ký tự');
        return;
    }
    
    if (!validateEmail(email)) {
        showError('emailError', 'Email không hợp lệ');
        return;
    }
    
    if (!validatePassword(password)) {
        showError('passwordError', 'Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }
    
    if (password !== passwordConfirmation) {
        showError('passwordConfirmationError', 'Mật khẩu xác nhận không khớp');
        return;
    }
    
    try {
        const response = await api.post('/auth/register', {
            name,
            email,
            password,
            passwordConfirmation
        });
        
        if (response && response.success) {
            if (response.data && response.data.token) {
                localStorage.setItem('token', response.data.token);
                if (response.data.user) {
                    localStorage.setItem('user', JSON.stringify(response.data.user));
                }
            }
            
            showToast('Đăng ký thành công!', 'success');
            
            // Redirect to homepage
            setTimeout(() => {
                sessionStorage.clear();
                window.location.replace('/');
            }, 500);
        }
    } catch (error) {
        showError('emailError', error.message || 'Đăng ký thất bại');
    }
}

async function handleForgotPassword(e) {
    e.preventDefault();
    
    const form = e.target;
    const email = form.email.value;
    
    clearErrors();
    
    if (!validateEmail(email)) {
        showError('emailError', 'Email không hợp lệ');
        return;
    }
    
    try {
        const response = await api.post('/auth/forgot-password', { email });
        
        if (response && response.success) {
            showToast('Email đã được gửi! Vui lòng kiểm tra hộp thư của bạn.', 'success');
            form.reset();
        }
    } catch (error) {
        showError('emailError', error.message || 'Gửi email thất bại');
    }
}

async function handleResetPassword(e) {
    e.preventDefault();
    
    const form = e.target;
    const email = form.email.value;
    const token = form.token.value;
    const password = form.password.value;
    const passwordConfirmation = form.passwordConfirmation.value;
    
    clearErrors();
    
    if (!validateEmail(email)) {
        showError('emailError', 'Email không hợp lệ');
        return;
    }
    
    if (!validatePassword(password)) {
        showError('passwordError', 'Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }
    
    if (password !== passwordConfirmation) {
        showError('passwordConfirmationError', 'Mật khẩu xác nhận không khớp');
        return;
    }
    
    try {
        const response = await api.post('/auth/reset-password', {
            email,
            token,
            password,
            passwordConfirmation
        });
        
        if (response && response.success) {
            showToast('Đặt lại mật khẩu thành công!', 'success');
            setTimeout(() => {
                window.location.href = '/login';
            }, 1500);
        }
    } catch (error) {
        showError('emailError', error.message || 'Đặt lại mật khẩu thất bại');
    }
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        const input = document.querySelector(`#${elementId.replace('Error', '')}`);
        if (input) {
            input.classList.add('error');
        }
    }
}

function clearErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => el.textContent = '');
    
    const inputs = document.querySelectorAll('.form-input');
    inputs.forEach(input => input.classList.remove('error'));
}

function loginWithGoogle() {
    window.location.href = '/api/auth/google';
}

