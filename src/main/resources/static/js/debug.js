// Debug script - Check localStorage on page load
console.log('=== DEBUG: Page loaded ===');
console.log('Token in localStorage:', localStorage.getItem('token'));
console.log('User in localStorage:', localStorage.getItem('user'));
console.log('Auth buttons element:', document.querySelector('.auth-buttons'));
console.log('User profile element:', document.querySelector('.user-profile'));
