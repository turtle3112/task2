<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý công việc</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <style>
        body {
            background: linear-gradient(to right, #fdfbfb, #ebedee);
            min-height: 100vh;
            font-family: 'Segoe UI', sans-serif;
        }

        .status-column {
            min-height: 300px;
            border-radius: 8px;
            padding: 1rem;
        }

        .task-card {
            margin-bottom: 1rem;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
            border-left: 5px solid #0d6efd;
            animation: fadeIn 0.5s ease;
        }

        .task-card.done { border-left-color: #198754; }
        .task-card.inprogress { border-left-color: #ffc107; }
        .task-card.todo { border-left-color: #dc3545; }

        @keyframes fadeIn {
            from {opacity: 0; transform: translateY(10px);}
            to {opacity: 1; transform: translateY(0);}
        }

        .task-form {
            background: white;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 1.5rem;
        }
    </style>
</head>
<body>
<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>📋 Quản lý công việc</h2>
        <button class="btn btn-outline-danger" onclick="logout()">Đăng xuất</button>
    </div>

    <!-- Form tạo task -->
    <div class="task-form">
        <h5>Thêm công việc mới</h5>
        <div class="row g-3 align-items-center">
            <div class="col-md-4">
                <input type="text" id="title" class="form-control" placeholder="Tên công việc" required>
            </div>
            <div class="col-md-3">
                <input type="date" id="deadline" class="form-control">
            </div>
            <div class="col-md-3">
                <select id="status" class="form-select">
                    <option value="TODO">❗ Chưa làm</option>
                    <option value="IN_PROGRESS">🔄 Đang làm</option>
                    <option value="DONE">✅ Hoàn thành</option>
                </select>
            </div>
            <div class="col-md-2">
                <button class="btn btn-primary w-100" onclick="createTask()">+ Thêm</button>
            </div>
        </div>
    </div>

    <!-- Danh sách task -->
    <div class="row" id="taskContainer">
        <div class="col-md-4">
            <h5>❗ Chưa làm</h5>
            <div class="status-column bg-white" id="todoTasks"></div>
        </div>
        <div class="col-md-4">
            <h5>🔄 Đang làm</h5>
            <div class="status-column bg-white" id="inProgressTasks"></div>
        </div>
        <div class="col-md-4">
            <h5>✅ Hoàn thành</h5>
            <div class="status-column bg-white" id="doneTasks"></div>
        </div>
    </div>
</div>

<script>
const token = localStorage.getItem("token");
if (!token) {
    alert("Vui lòng đăng nhập trước.");
    window.location.href = "login.jsp";
}

// Đăng xuất
function logout() {
    localStorage.removeItem("token");
    window.location.href = "login.jsp";
}

// Tải danh sách task
async function loadTasks() {
    const res = await fetch("/api/tasks", {
        headers: { Authorization: "Bearer " + token }
    });

    if (!res.ok) {
        alert("Lỗi khi tải công việc");
        return;
    }

    const tasks = await res.json();
    renderTasks(tasks);
}

// Hiển thị task vào từng cột
function renderTasks(tasks) {
    document.getElementById("todoTasks").innerHTML = "";
    document.getElementById("inProgressTasks").innerHTML = "";
    document.getElementById("doneTasks").innerHTML = "";

    tasks.forEach(task => {
        const div = document.createElement("div");
        div.className = "task-card card p-2 " + getStatusClass(task.status);
        div.innerHTML = `
            <h6 class="mb-1">${task.title}</h6>
            <small>📅 Deadline: ${task.deadline}</small><br>
            <small>📝 Trạng thái: ${task.status}</small>
        `;

        if (task.status === "TODO") {
            document.getElementById("todoTasks").appendChild(div);
        } else if (task.status === "IN_PROGRESS") {
            document.getElementById("inProgressTasks").appendChild(div);
        } else {
            document.getElementById("doneTasks").appendChild(div);
        }
    });
}

function getStatusClass(status) {
    return status === "DONE" ? "done" : status === "IN_PROGRESS" ? "inprogress" : "todo";
}

// Tạo task mới
async function createTask() {
    const title = document.getElementById("title").value.trim();
    const deadline = document.getElementById("deadline").value;
    const status = document.getElementById("status").value;

    if (!title) {
        alert("Vui lòng nhập tên công việc");
        return;
    }

    const res = await fetch("/api/tasks", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token
        },
        body: JSON.stringify({ title, deadline, status })
    });

    if (res.ok) {
        loadTasks();
        document.getElementById("title").value = "";
        document.getElementById("deadline").value = "";
        document.getElementById("status").value = "TODO";
    } else {
        alert("Lỗi tạo công việc");
    }
}

loadTasks();
</script>
</body>
</html>
