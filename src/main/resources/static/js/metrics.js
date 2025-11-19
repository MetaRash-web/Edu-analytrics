// Бизнес-логика для работы с метриками
const metrics = {
    // Расчет цветов для метрик
    getMetricColor: (value, thresholds = { good: 0.7, medium: 0.3 }) => {
        if (value >= thresholds.good) return '#28a745';
        if (value >= thresholds.medium) return '#ffc107';
        return '#dc3545';
    },

    // Анализ LTV/CAC соотношения
    analyzeLtvCacRatio: (ltv, cac) => {
        const ratio = ltv / cac;
        if (ratio >= 3) return { status: 'excellent', message: 'Отличное соотношение' };
        if (ratio >= 1.5) return { status: 'good', message: 'Хорошее соотношение' };
        return { status: 'poor', message: 'Низкое соотношение' };
    },

    // Прогноз выручки
    forecastRevenue: (currentRevenue, growthRate, periods = 12) => {
        const forecast = [];
        let revenue = currentRevenue;

        for (let i = 0; i < periods; i++) {
            revenue *= (1 + growthRate);
            forecast.push({
                period: i + 1,
                revenue: revenue
            });
        }

        return forecast;
    }
};