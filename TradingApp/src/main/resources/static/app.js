const pricesTable = document.querySelector('#prices tbody');
const historyTable = document.querySelector('#history tbody');
const holdingsTable = document.querySelector('#holdings tbody');
const balanceDiv = document.querySelector('#balance');
const holdingsCanvas = document.getElementById('holdingsChart');
let holdingsChart;

async function fetchPrices() {
    const res = await fetch('/api/prices');
    const data = await res.json();
    pricesTable.innerHTML = '';
    data.forEach(p => {
        const tr = document.createElement('tr');

        const safe = p.symbol.replace(/[^\w-]/g, '-');
        tr.innerHTML = `<td>${p.symbol}</td><td>${p.price.toFixed(2)}</td>` +
            `<td><input type='number' min='0' id='amt-${safe}' style='width:80px'></td>` +

            `<td><button onclick="trade('${p.symbol}','buy',${p.price})">Buy</button>` +
            `<button onclick="trade('${p.symbol}','sell',${p.price})">Sell</button></td>`;
        pricesTable.appendChild(tr);
    });
}

async function updateBalance() {
    const res = await fetch('/api/trade/balance');
    const bal = await res.text();
    balanceDiv.textContent = `Balance: $${parseFloat(bal).toFixed(2)}`;
}

async function fetchHoldings() {
    const res = await fetch('/api/trade/holdings');
    const data = await res.json();
    holdingsTable.innerHTML = '';
    data.forEach(h => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${h.symbol}</td><td>${h.quantity}</td>`;
        holdingsTable.appendChild(tr);
    });
    updateHoldingsChart(data);
}

async function fetchHistory() {
    const res = await fetch('/api/trade/history');
    if(!res.ok){
        console.error(await res.text());
        return;
    }
    const data = await res.json();
    historyTable.innerHTML = '';
    data.forEach(t => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${t.timestamp}</td><td>${t.type}</td><td>${t.symbol}</td>` +
            `<td>${t.amount}</td><td>${t.price}</td><td>${t.balanceAfter}</td>`;
        historyTable.appendChild(tr);
    });
}

function updateHoldingsChart(data){
    if(!holdingsCanvas) return;
    const labels = data.map(h => h.symbol);
    const quantities = data.map(h => h.quantity);
    if(!holdingsChart){
        holdingsChart = new Chart(holdingsCanvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Holdings',
                    data: quantities,
                    backgroundColor: 'rgba(75,192,192,0.4)',
                    borderColor: 'rgba(75,192,192,1)',
                    borderWidth: 1
                }]
            },
            options: {scales:{y:{beginAtZero:true}}}
        });
    } else {
        holdingsChart.data.labels = labels;
        holdingsChart.data.datasets[0].data = quantities;
        holdingsChart.update();
    }
}

async function trade(symbol, type, price) {

    const safe = symbol.replace(/[^\w-]/g, '-');
    const input = document.querySelector(`#amt-${safe}`);
    const amt = input && input.value;

    if (!amt || amt <= 0) return alert('Enter amount');
    const res = await fetch(`/api/trade/execute?price=${price}`, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({symbol, amount: parseFloat(amt), type})
    });
    if(!res.ok){
        const msg = await res.text();
        alert(msg);
    } else {
        await updateAll();
    }
}

async function reset() {
    await fetch('/api/trade/reset', {method:'POST'});
    await updateAll();
}

document.querySelector('#reset').addEventListener('click', reset);

async function updateAll(){
    await Promise.all([fetchPrices(), updateBalance(), fetchHoldings(), fetchHistory()]);
}

updateAll();
setInterval(fetchPrices, 5000);
