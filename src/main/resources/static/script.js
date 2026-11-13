// icons for each category
const ICONS = {Housing: "🏠", Insurance: "🛡️", Food: "🛒", Transport: "🚌", Subscriptions: "📺", Fun: "🎉", Other: "📦"};

// 1234.5 -> "1.235 kr"
function kr(number) {
    return Math.round(number).toLocaleString("da-DK") + " kr";
}

// Shows all expenses in the bottom
async function loadExpenses() {
    const expenses = await (await fetch("/api/expenses")).json();

    let rows = "";
    for (const e of expenses) {
        rows += `<tr>
            <td>${e.date}</td>
            <td>${e.description} ${e.fixed ? "(fixed)" : ""}</td>
            <td><span class="chip">${ICONS[e.category]} ${e.category}</span></td>
            <td><b>${kr(e.amount)}</b></td>
            <td><button class="delete" onclick="deleteExpense(${e.id})">✕</button></td>
        </tr>`;
    }
    document.getElementById("list").innerHTML = rows;
    loadSummary();
}

// Shows the key numbers, advice and category bars
async function loadSummary() {
    const income = Number(document.getElementById("income").value);
    localStorage.setItem("income", income);
    const s = await (await fetch("/api/summary?income=" + income)).json();

    const leftOver = income - s.estimate;
    document.getElementById("statIncome").textContent = kr(income);
    document.getElementById("estimate").textContent = kr(s.estimate);
    document.getElementById("leftOver").textContent = kr(leftOver);
    document.getElementById("leftOver").className = leftOver >= 0 ? "positive" : "negative";

    document.getElementById("advice").innerHTML = s.advice.map(a => `<li>💡 ${a}</li>`).join("");

    // the biggest category gets a full bar, the others are compared to it
    const max = Math.max(1, ...Object.values(s.perCategory));
    let bars = "";
    for (const [category, sum] of Object.entries(s.perCategory)) {
        bars += `<div class="bar-row">
            <div class="label"><span>${ICONS[category]} ${category}</span><b>${kr(sum)}</b></div>
            <div class="bar"><div style="width:${sum / max * 100}%"></div></div>
        </div>`;
    }
    document.getElementById("categories").innerHTML = bars;
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

// Picture -> Java -> OCR API -> fills in the form
async function uploadReceipt() {
    const file = document.getElementById("receipt").files[0];
    const status = document.getElementById("status");
    status.textContent = "⏳ Reading receipt...";

    const form = new FormData();
    form.append("image", await shrinkImage(file));
    const response = await fetch("/api/receipt", {method: "POST", body: form});
    if (!response.ok) {
        status.textContent = "❌ Could not read the receipt";
        return;
    }

    const r = await response.json();
    document.getElementById("description").value = r.description;
    document.getElementById("amount").value = r.amount;
    document.getElementById("category").value = r.category;
    if (r.date) document.getElementById("date").value = r.date;
    status.textContent = "✅ Check the values and press Add expense";
}

// Phone photos are too big for the free API (max 1 MB), so I make them smaller
function shrinkImage(file) {
    return new Promise(resolve => {
        const img = new Image();
        img.onload = () => {
            const scale = Math.min(1, 1600 / Math.max(img.width, img.height));
            const canvas = document.createElement("canvas");
            canvas.width = img.width * scale;
            canvas.height = img.height * scale;
            canvas.getContext("2d").drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob(resolve, "image/jpeg", 0.8);
        };
        img.src = URL.createObjectURL(file);
    });
}

// When the page opens
document.getElementById("income").value = localStorage.getItem("income") || "";
loadExpenses();
