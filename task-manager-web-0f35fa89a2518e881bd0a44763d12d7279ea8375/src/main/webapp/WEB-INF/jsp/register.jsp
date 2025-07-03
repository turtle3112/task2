<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng ký tài khoản</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <style>
        body {
            background: linear-gradient(to right, #74ebd5, #ACB6E5);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            font-family: 'Segoe UI', sans-serif;
        }

        .card {
            border: none;
            border-radius: 1rem;
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
            animation: fadeIn 0.6s ease-in-out;
        }

        @keyframes fadeIn {
            from {opacity: 0; transform: translateY(20px);}
            to {opacity: 1; transform: translateY(0);}
        }

        .btn-success {
            background-color: #28a745;
            border: none;
        }

        .btn-success:hover {
            background-color: #218838;
        }

        .form-label {
            font-weight: bold;
        }

        h3 {
            color: #333;
        }

        a {
            color: #007bff;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<div class="container" style="max-width: 420px;">
    <div class="card p-4">
        <h3 class="text-center mb-3">Đăng ký tài khoản</h3>
        <form id="registerForm">
            <div class="mb-3">
                <label for="username" class="form-label">Tên đăng nhập</label>
                <input type="text" class="form-control" id="username" placeholder="vd: tan.nguyen" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Mật khẩu</label>
                <input type="password" class="form-control" id="password" placeholder="Tối thiểu 6 ký tự" required>
            </div>
            <button type="submit" class="btn btn-success w-100">Đăng ký</button>
            <p class="mt-3 text-center">Đã có tài khoản? <a href="login.jsp">Đăng nhập</a></p>
        </form>
    </div>
</div>

<script>
document.getElementById("registerForm").addEventListener("submit", async function(e) {
    e.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    const res = await fetch("/auth/v2/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    });

    if (res.ok) {
        alert("🎉 Đăng ký thành công. Chuyển sang đăng nhập.");
        window.location.href = "login.jsp";
    } else {
        const data = await res.json();
        alert("❌ " + (data.message || "Đăng ký thất bại!"));
    }
});
</script>
</body>
</html>
