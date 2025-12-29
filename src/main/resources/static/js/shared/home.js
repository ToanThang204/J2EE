// Home Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Clear any pending redirects when landing on homepage
    sessionStorage.removeItem('redirectUrl');
    
    initHeroSection();
    initJobSearch();
    initPopupPositioning();
    // Không cần loadRecentJobs() vì Thymeleaf đã render sẵn
});

function initHeroSection() {
    const heroSearchInput = document.getElementById('heroSearchInput');
    const heroSearchBtn = document.getElementById('heroSearchBtn');
    
    if (heroSearchInput && heroSearchBtn) {
        heroSearchBtn.addEventListener('click', () => {
            const searchTerm = heroSearchInput.value.trim();
            if (searchTerm) {
                window.location.href = `/jobs?search=${encodeURIComponent(searchTerm)}`;
            }
        });
        
        heroSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                heroSearchBtn.click();
            }
        });
    }
}

function initJobSearch() {
    const searchInput = document.getElementById('jobSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const searchTerm = e.target.value.trim();
                if (searchTerm) {
                    window.location.href = `/jobs?search=${encodeURIComponent(searchTerm)}`;
                }
            }
        });
    }
}

// Không cần loadRecentJobs() và displayJobs() nữa
// Thymeleaf đã render sẵn job cards với full thông tin từ server

function initPopupPositioning() {
    const POPUP_MARGIN = 40; // space from window edge
    const cards = document.querySelectorAll('.job-card');
    if (!cards || cards.length === 0) return;

    function updateCard(card) {
        const popup = card.querySelector('.job-detail-popup');
        if (!popup) return;
        const rect = card.getBoundingClientRect();
        const popupWidth = popup.offsetWidth || 350;
        const spaceRight = window.innerWidth - rect.right;
        if (spaceRight < popupWidth + POPUP_MARGIN) {
            popup.classList.add('popup-left');
        } else {
            popup.classList.remove('popup-left');
        }
    }

    cards.forEach(card => {
        card.addEventListener('mouseenter', () => updateCard(card));
        // also update on mousemove to handle dynamic layout
        card.addEventListener('mousemove', () => updateCard(card));
    });

    window.addEventListener('resize', () => {
        cards.forEach(card => updateCard(card));
    });
}

