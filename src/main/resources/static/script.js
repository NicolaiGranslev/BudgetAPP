// Remember the income in the browser
document.getElementById("income").value = localStorage.getItem("income") || "";

async function loadExpenses() {
    const expenses = await (await fetch("/api/expenses")).json();
    const list = document.getElementById("list");
    list.innerHTML = "";
    for (const e of expenses) {
        list.innerHTML += `<tr>
            <td>${e.date}</td>
            <td>${e.description}${e.fixed ? " (fixed)" : ""}</td>
            <td>${e.category}</td>
            <td>${e.amount} kr</td>
            <td><button onclick="deleteExpense(${e.id})">X</button></td>
        </tr>`;
    }
    loadSummary();
}

async function loadSummary() {
    const income = document.getElementById("income").value || 0;
    localStorage.setItem("income", income);
    const s = await (await fetch("/api/summary?income=" + income)).json();

    document.getElementById("estimate").textContent = s.estimate;
    document.getElementById("advice").innerHTML = s.advice.map(a => `<li>${a}</li>`).join("");
    document.getElementById("categories").innerHTML = Object.entries(s.perCategory)
        .map(([cat, sum]) => `<li>${cat}: ${sum} kr</li>`).join("");
}

async function addExpense() {
    const expense = {
        description: document.getElementById("description").value,
        amount: document.getElementById("amount").value,
        category: document.getElementById("category").value,
        date: document.getElementById("date").value || null,
        fixed: document.getElementById("fixed").checked
    };
    await fetch("/api/expenses", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(expense)
    });
    document.getElementById("description").value = "";
    document.getElementById("amount").value = "";
    loadExpenses();
}

async function deleteExpense(id) {
    await fetch("/api/expenses/" + id, {method: "DELETE"});
    loadExpenses();
}

// Sends the picture to Java, which calls the OCR API, then fills in the form
async function uploadReceipt() {
    const file = document.getElementById("receipt").files[0];
    if (!file) return;
    document.getElementById("status").textContent = "Reading receipt...";

    const form = new FormData();
    form.append("image", file);
    const r = await (await fetch("/api/receipt", {method: "POST", body: form})).json();

    document.getElementById("description").value = r.description;
    document.getElementById("amount").value = r.amount;
    document.getElementById("category").value = r.category;
    document.getElementById("status").textContent = "Check the values and press Add";
}

loadExpenses();
