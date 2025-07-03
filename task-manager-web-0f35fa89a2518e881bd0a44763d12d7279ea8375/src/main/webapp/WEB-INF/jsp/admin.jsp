<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            background: linear-gradient(to right, #8360c3, #2ebf91);
            min-height: 100vh;
            color: white;
            font-family: 'Segoe UI', sans-serif;
        }

        .card {
            border-radius: 1rem;
            box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
            margin-bottom: 1rem;
        }

        .card-body {
            background: white;
            color: black;
        }

        h3, h5 {
            color: #fff;
        }

        .btn-logout {
            position: absolute;
            top: 1rem;
            right: 2rem;
        }

        table {
            background-color: white;
        }

        th, td {
            text-align: center;
        }
    </style>
</head>
<body>
<div class="container py-4">
    <button class="btn btn-light btn-sm btn-logout" onclick="logout()">Đăng xuất</button>
    <h3 class="text-center mb-4">👑 Admin Dashboard</h3>

    <!-- Tổng quan -->
    <div class="row text-center mb-4">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5>Tổng người dùng</h5>
                    <p id="userCount" class="fs-3">0</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5>Tổng công việc</h5>
                    <p id="taskCount" class="fs-3">0</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5>Biểu đồ trạng thái</h5>
                    <canvas id="statusChart" height="120"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Danh sách người dùng -->
    <div class="card">
        <div class="card-body">
            <h5 class="mb-3">📋 Danh sách người dùng</h5>
            <table class="table table-bordered table-hover">
                <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Tên đăng nhập</th>
                        <th>Vai trò</th>
                    </tr>
                </thead>
                <tbody id="userTable"></tbody>
            </table>
        </div>
    </div>
</div>

<script>
const token = localStorage.getItem("token");
if (!token) {
    alert("Bạn chưa đăng nhập");
    window.location.href = "login.jsp";
}

// Đăng xuất
function logout() {
    localStorage.removeItem("token");
    window.location.href = "login.jsp";
}

// Tải danh sách user
async function loadUsers() {
    const res = await fetch("/api/users", {
        headers: { Authorization: "Bearer " + token }
    });

    if (res.ok) {
        const users = await res.json();
        document.getElementById("userCount").textContent = users.length;

        const tbody = document.getElementById("userTable");
        tbody.innerHTML = "";
        users.forEach((user, i) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `<td>${i + 1}</td><td>${user.username}</td><td>${user.role}</td>`;
            tbody.appendChild(tr);
        });
    } else {
        alert("Không thể tải danh sách người dùng");
    }
}

// Tải task để thống kê
async function loadTaskStats() {
    const res = await fetch("/api/tasks", {
        headers: { Authorization: "Bearer " + token }
    });

    if (!res.ok) return;

    const tasks = await res.json();
    document.getElementById("taskCount").textContent = tasks.length;

    const statusCount = { TODO: 0, IN_PROGRESS: 0, DONE: 0 };
    tasks.forEach(t => statusCount[t.status]++);

    new Chart(document.getElementById("statusChart").getContext("2d"), {
        type: 'pie',
        data: {
            labels: ["❗ Chưa làm", "🔄 Đang làm", "✅ Hoàn thành"],
            datasets: [{
                data: [statusCount.TODO, statusCount.IN_PROGRESS, statusCount.DONE],
                backgroundColor: ["#dc3545", "#ffc107", "#198754"]
            }]
        }
    });
}

loadUsers();
loadTaskStats();
</script>
</body>
</html>
