(function () {
    const scrollTopKey = "stocktracker.inventory.scrollTop";
    const restoreRequestedKey = "stocktracker.inventory.restoreScroll";

    function storage() {
        try {
            return window.sessionStorage;
        } catch (error) {
            return null;
        }
    }

    function scrollContainer() {
        return document.querySelector("[data-inventory-scroll-container]");
    }

    function saveInventoryScroll() {
        const container = scrollContainer();
        const sessionStorage = storage();
        if (!container || !sessionStorage) {
            return;
        }

        sessionStorage.setItem(scrollTopKey, String(container.scrollTop));
        sessionStorage.setItem(restoreRequestedKey, "true");
    }

    function restoreInventoryScroll() {
        const container = scrollContainer();
        const sessionStorage = storage();
        if (!container || !sessionStorage || sessionStorage.getItem(restoreRequestedKey) !== "true") {
            return;
        }

        const scrollTop = Number.parseInt(sessionStorage.getItem(scrollTopKey) || "0", 10);
        if (!Number.isFinite(scrollTop)) {
            return;
        }

        window.requestAnimationFrame(function () {
            container.scrollTop = scrollTop;
            sessionStorage.removeItem(restoreRequestedKey);
        });
    }

    document.addEventListener("submit", function (event) {
        if (event.target instanceof Element && event.target.matches("[data-preserve-inventory-scroll]")) {
            saveInventoryScroll();
        }
    });

    document.addEventListener("click", function (event) {
        const trigger = event.target instanceof Element
            ? event.target.closest("[data-preserve-inventory-scroll], [data-restore-inventory-scroll]")
            : null;
        if (trigger) {
            saveInventoryScroll();
        }
    });

    window.addEventListener("pageshow", restoreInventoryScroll);
    document.addEventListener("DOMContentLoaded", restoreInventoryScroll);
})();
