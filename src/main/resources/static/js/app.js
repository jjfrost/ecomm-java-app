document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.querySelector("[data-nav-toggle]");
    const sidebar = document.querySelector(".sidebar");

    if (toggle && sidebar) {
        toggle.addEventListener("click", () => {
            sidebar.classList.toggle("sidebar-open");
        });
    }

    document.querySelectorAll(".pagination a.disabled").forEach((link) => {
        link.addEventListener("click", (event) => event.preventDefault());
        link.setAttribute("aria-disabled", "true");
        link.removeAttribute("href");
    });

    document.querySelectorAll("[data-line-chart]").forEach((chart, chartIndex) => {
        const values = (chart.dataset.values || "")
            .split(",")
            .filter(Boolean)
            .map(Number);
        const labels = (chart.dataset.labels || "").split("|").filter(Boolean);
        const svg = chart.querySelector("svg");
        if (!svg || values.length === 0) {
            return;
        }

        const namespace = "http://www.w3.org/2000/svg";
        const width = 620;
        const height = 230;
        const padding = {top: 18, right: 18, bottom: 34, left: 46};
        const plotWidth = width - padding.left - padding.right;
        const plotHeight = height - padding.top - padding.bottom;
        const maximum = Math.max(...values, 1);
        const ceiling = Math.ceil(maximum / 100) * 100;
        const gradientId = `revenue-area-gradient-${chartIndex}`;

        const create = (name, attributes = {}) => {
            const element = document.createElementNS(namespace, name);
            Object.entries(attributes).forEach(([key, value]) => {
                element.setAttribute(key, value);
            });
            return element;
        };

        const definitions = create("defs");
        const gradient = create("linearGradient", {
            id: gradientId,
            x1: "0",
            x2: "0",
            y1: "0",
            y2: "1"
        });
        gradient.append(
            create("stop", {"offset": "0%", "stop-color": "#6366f1", "stop-opacity": ".28"}),
            create("stop", {"offset": "100%", "stop-color": "#6366f1", "stop-opacity": "0"})
        );
        definitions.append(gradient);
        svg.append(definitions);

        for (let index = 0; index <= 4; index++) {
            const y = padding.top + (plotHeight * index / 4);
            svg.append(create("line", {
                x1: padding.left,
                x2: width - padding.right,
                y1: y,
                y2: y,
                class: "chart-grid-line"
            }));
            const value = Math.round(ceiling * (1 - index / 4));
            const label = create("text", {
                x: padding.left - 8,
                y: y + 3,
                "text-anchor": "end",
                class: "chart-axis-label"
            });
            label.textContent = `$${value}`;
            svg.append(label);
        }

        const points = values.map((value, index) => {
            const x = padding.left
                + (values.length === 1 ? plotWidth / 2 : plotWidth * index / (values.length - 1));
            const y = padding.top + plotHeight - (value / ceiling * plotHeight);
            return {x, y, value, label: labels[index] || ""};
        });
        const linePoints = points.map((point) => `${point.x},${point.y}`).join(" ");
        const areaPoints = [
            `${points[0].x},${padding.top + plotHeight}`,
            ...points.map((point) => `${point.x},${point.y}`),
            `${points[points.length - 1].x},${padding.top + plotHeight}`
        ].join(" ");

        svg.append(create("polygon", {
            points: areaPoints,
            fill: `url(#${gradientId})`
        }));
        svg.append(create("polyline", {
            points: linePoints,
            class: "chart-line"
        }));

        points.forEach((point) => {
            const circle = create("circle", {
                cx: point.x,
                cy: point.y,
                r: 4,
                class: "chart-point"
            });
            const title = create("title");
            title.textContent = `${point.label}: $${point.value.toFixed(2)}`;
            circle.append(title);
            svg.append(circle);
        });

        [...new Set([0, Math.floor((labels.length - 1) / 2), labels.length - 1])]
            .filter((index) => index >= 0)
            .forEach((index) => {
                const point = points[index];
                const label = create("text", {
                    x: point.x,
                    y: height - 8,
                    "text-anchor": index === 0 ? "start" : index === labels.length - 1 ? "end" : "middle",
                    class: "chart-axis-label"
                });
                label.textContent = labels[index];
                svg.append(label);
            });
    });
});
