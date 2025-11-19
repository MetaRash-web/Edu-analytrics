// Утилитарные функции
const utils = {
    // Форматирование чисел
    formatCurrency: (amount, currency = '₽') => {
        return currency + amount.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
    },

    formatNumber: (num) => {
        return new Intl.NumberFormat('ru-RU').format(num);
    },

    formatPercent: (value) => {
        return value.toFixed(1) + '%';
    },

    // Работа с датами
    formatDate: (dateString) => {
        return new Date(dateString).toLocaleDateString('ru-RU');
    },

    debounce: (func, delay) => {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), delay);
        };
    },

    // API запросы
    api: {
        async get(url, params = {}) {
            const queryString = new URLSearchParams(params).toString();
            const response = await fetch(`${url}?${queryString}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        },

        async post(url, data) {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        }
    },

    // Обработка ошибок
    handleError: (error, userMessage = 'Произошла ошибка') => {
        console.error('Error:', error);
        alert(userMessage);
    }
};