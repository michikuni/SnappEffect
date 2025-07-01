@echo off
set /p choice="Chọn tài khoản Git (a, b, c): "

if /i "%choice%"=="a" (
    git config user.name "michikuni"
    git config user.email "minhphuonglcby@gmail.com"
    echo Đã chọn tài khoản Phương
) else if /i "%choice%"=="b" (
    git config user.name "ducnguyen-source"
    git config user.email "minhphuongcy6kma1@gmail.com"
    echo Đã chọn tài khoản duc
) else if /i "%choice%"=="c" (
    git config user.name "dungtran123-cyber"
    git config user.email "dangminhphuong20042003@gmail.com"
    echo Đã chọn tài khoản dung
) else (
    echo Lựa chọn không hợp lệ.
)
pause
