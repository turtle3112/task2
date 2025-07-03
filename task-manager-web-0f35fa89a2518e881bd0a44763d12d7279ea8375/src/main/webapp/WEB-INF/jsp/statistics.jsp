<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thống kê công việc</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            background: linear-gradient(to right, #d7d2cc, #304352);
            color: white;
            min-height: 100vh;
            font-family: 'Segoe UI', sans-serif;
        }

        .container {
            max-width: 800px;
        }

        .card {
            background: white;
            color: black;
            border-radius: 1rem;
            padding: 2rem;
            box-shadow: 0 6px 18px rgba(0,0,0,0.2);
        }

        .btn-back {
            margin-top: 1rem;
        }
    </style>
</head>
<body>
<div class="container py-5">
    <h2 class="text-center mb-4">📊 Thống kê công việc</h2>

    <div class="card">
        <canvas id="taskChart" height="200"></canvas>
    </div>

    <div class="text-center btn-back">
        <a href="tasks.jsp" class="btn btn-secondary">← Quay lại danh sách công việc</a>
    </div>
</div>

<script>
const token = localStorage.getItem("token");
if (!token) {
    alert("Vui lòng đăng nhập trước.");
    window.location.href = "login.jsp";
}

async function loadTaskStats() {
    const res = await fetch("/api/tasks", {
        headers: { Authorization: "Bearer " + token }
    });

    if (!res.ok) {
        alert("Lỗi tải dữ liệu thống kê");
        return;
    }

    const tasks = await res.json();

    const statusCount = { TODO: 0, IN_PROGRESS: 0, DONE: 0 };
    tasks.forEach(task => statusCount[task.status]++);

    const ctx = document.getElementById("taskChart").getContext("2d");
    new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["❗ Chưa làm", "🔄 Đang làm", "✅ Hoàn thành"],
            datasets: [{
                label: "Số lượng công việc",
                data: [statusCount.TODO, statusCount.IN_PROGRESS, statusCount.DONE],
                backgroundColor: ["#dc3545", "#ffc107", "#198754"]
            }]
        },
        options: {
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } }
            }
        }
    });
}

loadTaskStats();
</script>
</body>
</html>
