(function () {
    function formState(form) {
        return Array.from(form.elements)
            .filter(function (element) {
                return element instanceof HTMLInputElement ||
                    element instanceof HTMLSelectElement ||
                    element instanceof HTMLTextAreaElement;
            })
            .map(function (element) {
                return {
                    name: element.name,
                    type: element.type,
                    value: element.value,
                    checked: element instanceof HTMLInputElement ? element.checked : undefined,
                };
            });
    }

    function hasChanged(form, initialState) {
        return JSON.stringify(formState(form)) !== JSON.stringify(initialState);
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("[data-confirm-discard-changes]").forEach(function (backLink) {
            var form = document.getElementById(backLink.dataset.formId);
            if (!(form instanceof HTMLFormElement)) {
                return;
            }

            var initialState = formState(form);
            backLink.addEventListener("click", function (event) {
                if (!hasChanged(form, initialState)) {
                    return;
                }

                if (!window.confirm("Ungespeicherte Änderungen verwerfen?")) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            });
        });
    });
})();
