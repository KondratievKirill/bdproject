-- ============================================
-- Скрипт создания таблиц базы данных
-- Система учета личных финансов
-- SQL Server (T-SQL) с поддержкой кириллицы
-- ============================================

-- Удаляем таблицы в правильном порядке (сначала дочерние)
IF OBJECT_ID('dbo.savings_goals', 'U') IS NOT NULL DROP TABLE dbo.savings_goals;
IF OBJECT_ID('dbo.transactions', 'U') IS NOT NULL DROP TABLE dbo.transactions;
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID('dbo.accounts', 'U') IS NOT NULL DROP TABLE dbo.accounts;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;


-- ============================================
-- Таблица 1: users (Пользователи)
-- ============================================
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);


-- ============================================
-- Таблица 2: accounts (Счета)
-- ============================================
CREATE TABLE accounts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    name NVARCHAR(100) NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_accounts_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_balance_non_negative 
        CHECK (balance >= 0)
);


-- ============================================
-- Таблица 3: categories (Категории)
-- ============================================
CREATE TABLE categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    name NVARCHAR(100) NOT NULL,
    type NVARCHAR(10) NOT NULL,
    CONSTRAINT fk_categories_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_category_type 
        CHECK (type IN ('income', 'expense'))
);


-- ============================================
-- Таблица 4: transactions (Транзакции)
-- ============================================
CREATE TABLE transactions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description NVARCHAR(255),
    CONSTRAINT fk_transactions_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    -- ВАЖНО: без ON DELETE CASCADE, чтобы избежать конфликта путей
    CONSTRAINT fk_transactions_account 
        FOREIGN KEY (account_id) 
        REFERENCES accounts(id),
    CONSTRAINT fk_transactions_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_amount_positive 
        CHECK (amount > 0)
);


-- ============================================
-- Таблица 5: savings_goals (Цели накоплений)
-- ============================================
CREATE TABLE savings_goals (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    name NVARCHAR(100) NOT NULL,
    target_amount DECIMAL(10, 2) NOT NULL,
    current_amount DECIMAL(10, 2) DEFAULT 0.00,
    deadline DATE,
    CONSTRAINT fk_goals_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_current_amount_non_negative 
        CHECK (current_amount >= 0),
    CONSTRAINT chk_target_amount_positive 
        CHECK (target_amount > 0)
);


-- ============================================
-- Создаем индексы для ускорения поиска
-- ============================================
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_savings_goals_user_id ON savings_goals(user_id);


-- ============================================
-- Проверка создания таблиц
-- ============================================
SELECT 'Таблицы успешно созданы!' AS result;
SELECT 
    TABLE_NAME, 
    COUNT(*) as column_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'dbo' 
    AND TABLE_NAME IN ('users', 'accounts', 'categories', 'transactions', 'savings_goals')
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;
