<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>index</title>
    <link rel="stylesheet" href="./static/css/style.css">
</head>
<body>

<div class="container">
    <form action="/register" method="post" class="signup-form">
        <div class="title">serv00 注册</div>
        <div class="form-group">
            <label for="email">邮箱:</label>
            <input type="text" id="email" name="email" placeholder="请输入邮箱">
        </div>
        <div class="form-group">
            <label for="threadCount">线程数:</label>
            <input type="text" id="threadCount" name="threadCount" placeholder="请输入启用线程数">
        </div>
        <div class="form-group">
            <label for="registerCount">注册次数:</label>
            <input type="text" id="registerCount" name="registerCount" placeholder="请输入注册次数">
        </div>
        <div class="button-group">
            <input type="submit" id="register" value="注册">
            <input type="submit" id="registerBatch" value="批量注册">
        </div>
    </form>

    <div class="title">注册结果</div>
    <div class="result-container">
        <div class="result" id="result-display">
            <#--这里是注册结果-->
        </div>
    </div>
</div>

<script>
    // 注册按钮点击事件
    document.querySelector('input[id="register"]').onclick = function () {
        const email = document.querySelector('input[name="email"]').value;
        fetch('/register', {
            method: 'post',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: 'email=' + email
        }).then(response => response.json())
            .then(data => {
                console.log(data);
                document.getElementById('result-display').innerHTML = data.data;
            });
        return false;
    };

    // 批量注册按钮点击事件
    document.querySelector('input[id="registerBatch"]').onclick = function () {
        const email = document.querySelector('input[name="email"]').value;
        const threadCount = document.querySelector('input[name="threadCount"]').value;
        const registerCount = document.querySelector('input[name="registerCount"]').value;
        const resultDiv = document.getElementById('result-display');

        fetch('/registerBatch', {
            method: 'post',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: 'email=' + email + '&threadCount=' + threadCount + '&registerCount=' + registerCount
        }).then(response =>{
            if (!response.ok) {
                throw new Error(`HTTP error! status`);
            }
            const reader = response.body.getReader();
            const textDecoder = new TextDecoder();

            const read = () => {
                reader.read().then(({ done, value }) => {
                    if (done) {
                        console.log('Stream 结束');
                        return;
                    }
                    const chunk = textDecoder.decode(value);
                    resultDiv.innerHTML += chunk;
                    resultDiv.scrollTop = resultDiv.scrollHeight; // 自动滚动到底部
                    read();
                }).catch(error => {
                    console.error('读取流时发生错误:', error);
                });
            };
            resultDiv.innerHTML = ''; // 清空之前的注册结果
            read(); // 开始读取流
        });
        return false;
    };
</script>

</body>
</html>
