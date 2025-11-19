const charts = {
    chartsInstances: {},

    init(data) {
        this.renderAudienceChart(data.audienceMetrics);
        this.renderRetentionChart(data.retentionTrend);

        // Новый: добавь ресайз для адаптивности
        window.addEventListener('resize', () => {
            if (this.chartsInstances.audience) this.chartsInstances.audience.update();
            if (this.chartsInstances.retention) this.chartsInstances.retention.update();
        });
    },

    renderAudienceChart(audienceMetrics) {
        const el = document.getElementById('audienceChart');
        if (!el || !audienceMetrics?.DAU) {
            el && (el.innerHTML = '<div class="text-muted text-center mt-3">Нет данных для графика</div>');
            return;
        }

        const dates = Object.keys(audienceMetrics.DAU).sort();
        const dau = dates.map(d => audienceMetrics.DAU[d] ?? 0);
        const wau = dates.map(d => audienceMetrics.WAU[d] ?? 0);
        const mau = dates.map(d => audienceMetrics.MAU[d] ?? 0);

        // Убрал: старый step и labelInterp
        // Заменил на: smartLabelInterpolation (теперь метки умные и адаптивные)
        const labelInterp = (value, index) => smartLabelInterpolation(dates, index);

        this.chartsInstances.audience = new Chartist.Line('#audienceChart', {
            labels: dates,
            series: [dau, wau, mau]
        }, {
            fullWidth: true,
            lineSmooth: Chartist.Interpolation.cardinal({ tension: 0.1 }), // Изменил: tension на 0.1 для большего сглаживания (меньше зигзагов)
            axisX: {
                labelInterpolationFnc: labelInterp,
                offset: 60,
                showGrid: false
            },
            axisY: {
                labelInterpolationFnc: v => utils.formatNumber(v),
                onlyInteger: true
            },
        });

        this.addLegend('audienceChart', ['DAU (синий)', 'WAU (зелёный)', 'MAU (жёлтый)']);
    },

    renderRetentionChart(retentionTrend) {
        const el = document.getElementById('retentionChart');
        if (!el || !retentionTrend) {
            el && (el.innerHTML = '<div class="text-muted text-center mt-3">Нет данных для графика</div>');
            return;
        }

        const dates = Object.keys(retentionTrend).sort();
        const values = dates.map(d => retentionTrend[d] ?? 0);

        // Убрал: старый step и labelInterp
        // Заменил на: smartLabelInterpolation
        const labelInterp = (value, index) => smartLabelInterpolation(dates, index);

        this.chartsInstances.retention = new Chartist.Line('#retentionChart', {
            labels: dates,
            series: [values]
        }, {
            fullWidth: true,
            lineSmooth: Chartist.Interpolation.cardinal({ tension: 0.1 }), // Добавил: сглаживание
            low: 0,
            high: 100,
            axisX: {
                labelInterpolationFnc: labelInterp,
                offset: 90,
                showGrid: false
            },
            axisY: {
                labelInterpolationFnc: v => v + '%',
                onlyInteger: true
            }
        });

        this.addLegend('retentionChart', ['Retention Rate']);
    },

    addLegend(chartId, items) {
        const chartEl = document.getElementById(chartId);
        if (!chartEl) return;

        let legendHtml = '<div class="chart-legend mt-3 d-flex justify-content-center gap-3 flex-wrap">';
        items.forEach((item, i) => {
            const color = ['#4361ee', '#2ecc71', '#f39c12'][i] || '#000';
            legendHtml += `<span class="legend-item" style="color: ${color}; font-weight: 500;">${item}</span>`;
        });
        legendHtml += '</div>';

        chartEl.insertAdjacentHTML('afterend', legendHtml);
    },

    destroy() {
        Object.values(this.chartsInstances).forEach(c => c && c.detach());
        this.chartsInstances = {};
        document.querySelectorAll('.ct-chart').forEach(e => e.innerHTML = '');
        // Убираем ручные элементы
        document.querySelectorAll('.chart-legend, .custom-tooltip').forEach(el => el.remove());
    }
};

// Функция smartLabelInterpolation — оставляем как есть, она топ

function smartLabelInterpolation(dates, index) {
    const total = dates.length;
    if (total <= 12) return utils.formatDate(dates[index]);

    // На широких экранах — каждые 30–40 дней
    // На узких — каждые 60–90 дней
    const isMobile = window.innerWidth < 992;
    const step = isMobile
        ? Math.ceil(total / 6)   // 6 меток максимум на мобиле
        : Math.ceil(total / 10); // 10 меток на десктопе

    return (index % step === 0 || index === total - 1)
        ? utils.formatDate(dates[index])
        : '';
}