// Основная логика дашборда
const dashboard = {
    currentData: null,

    init: function(data) {
        this.currentData = data;
        this.bindEvents();
        this.updateDashboard(data);
    },

    // Привязка событий
    bindEvents: function() {
        const periodSelect = document.getElementById('periodSelect');
        if (periodSelect) {
            // NEW: Добавил debounce для предотвращения спама запросами при быстром смене
            periodSelect.addEventListener('change', utils.debounce((e) => {
                this.handlePeriodChange(e.target.value);
            }, 300));
        }
    },

    // Обработка смены периода
    handlePeriodChange: async function(period) {
        const loadingSpinner = document.getElementById('loadingSpinner');
        if (loadingSpinner) loadingSpinner.style.display = 'block';

        try {
            // Обновляем URL без релоада
            const newUrl = new URL(window.location);
            newUrl.searchParams.set('period', period);
            window.history.replaceState({}, '', newUrl);

            // Загружаем новые данные через AJAX
            const newData = await this.loadDataForPeriod(period);

            // Обновляем интерфейс
            this.updateDashboard(newData);

        } catch (error) {
            console.error('Ошибка при загрузке данных:', error);
            alert('Ошибка при загрузке данных. Проверь консоль.');
        } finally {
            if (loadingSpinner) loadingSpinner.style.display = 'none';
        }
    },

    // Загрузка данных для периода (теперь AJAX)
    loadDataForPeriod: async function(period) {
        const response = await fetch(`/api/metrics?period=${period}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    },

    loadDataForPeriod: async function(period) {
        return utils.api.get('/api/metrics', { period });
    },

    // Рендер списка продуктов
    renderProducts: function(products) {
        const productsGrid = document.getElementById('productsGrid');

        if (!products || products.length === 0) {
            productsGrid.innerHTML = '<p>Нет продуктов для отображения</p>';
            return;
        }

        productsGrid.innerHTML = products.map(product => `
            <div class="col-md-4">
                <div class="product-card">
                    <div class="product-name">${product.courseName}</div>
                    <div class="product-stats">
                        <span>Продажи: ${utils.formatNumber(product.salesCount)}</span>
                        <span>Выручка: ${utils.formatCurrency(product.revenue)}</span>
                    </div>
                </div>
            </div>
        `).join('');
    },

    // Обновление всех данных дашборда
    updateDashboard: function(newData) {
        this.currentData = newData;

        const stats = newData.dashboardStats || {};
        const retentionRate = newData.retentionRate || 0;
        const ltv = newData.ltv || 0;
        const cac = newData.cac || 0;
        const arppu = newData.arppu || 0;

        document.getElementById('userCount').textContent = utils.formatNumber(stats.userCount) || 0;
        document.getElementById('courseCount').textContent = utils.formatNumber(stats.courseCount) || 0;
        document.getElementById('orderCount').textContent = utils.formatNumber(stats.orderCount) || 0;
        document.getElementById('totalRevenue').textContent = utils.formatCurrency(stats.totalRevenue) || 0;
        document.getElementById('retentionRate').textContent = utils.formatPercent(retentionRate) || 0;
        document.getElementById('ltv').textContent = utils.formatCurrency(ltv) || 0;
        document.getElementById('cac').textContent = utils.formatCurrency(cac) || 0;
        document.getElementById('arppu').textContent = utils.formatCurrency(arppu) || 0;

        this.renderProducts(newData.productPerformance);
        charts.destroy();
        charts.init(newData);
    },

    // Экспорт данных
    exportData: function(format = 'json') {
        if (!this.currentData) return;

        if (format === 'json') {
            const dataStr = JSON.stringify(this.currentData, null, 2);
            const dataBlob = new Blob([dataStr], { type: 'application/json' });
            this.downloadBlob(dataBlob, 'dashboard-data.json');
        } else if (format === 'csv') {
            // Пример для продуктов, можно расширить
            let csvContent = "courseId,courseName,salesCount,revenue\n";
            this.currentData.productPerformance.forEach(product => {
                const escapedName = `"${product.courseName.replace(/"/g, '""')}"`; // Стандартное экранирование CSV
                csvContent += `${product.courseId || ''},${escapedName},${product.salesCount},${product.revenue}\n`;
            });
            const dataBlob = new Blob([csvContent], { type: 'text/csv' });
            this.downloadBlob(dataBlob, 'dashboard-products.csv');
        }
    },

    // Вспомогательная функция для скачивания
    downloadBlob: function(blob, filename) {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }
};

// Глобальный экспорт для отладки
window.dashboard = dashboard;