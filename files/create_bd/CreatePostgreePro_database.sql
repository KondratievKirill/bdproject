-- ============================================
-- Создание таблиц для PostgreSQL
-- Система учета личных финансов
-- ============================================

-- Удаляем таблицы
DROP TABLE IF EXISTS savings_goals CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Таблица пользователей
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица счетов
CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_accounts_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_balance_non_negative 
        CHECK (balance >= 0)
);

-- Таблица категорий (ОБЩАЯ для всех)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    CONSTRAINT chk_category_type 
        CHECK (type IN ('income', 'expense'))
);

-- Таблица транзакций (БЕЗ user_id)
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_transactions_account 
        FOREIGN KEY (account_id) 
        REFERENCES accounts(id),
    CONSTRAINT fk_transactions_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id),
    CONSTRAINT chk_amount_positive 
        CHECK (amount > 0),
    CONSTRAINT chk_transaction_date_not_future
        CHECK (transaction_date <= CURRENT_DATE)
);

-- Таблица целей
CREATE TABLE savings_goals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
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

-- Индексы
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_savings_goals_user_id ON savings_goals(user_id);

SELECT 'Таблицы успешно созданы!' AS result;
