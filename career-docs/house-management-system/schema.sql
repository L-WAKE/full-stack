CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_code VARCHAR(64) NOT NULL,
    menu_name VARCHAR(64) NOT NULL,
    menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU',
    path VARCHAR(128),
    permission_code VARCHAR(64),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE landlord (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    name VARCHAR(64) NOT NULL,
    mobile VARCHAR(32) NOT NULL,
    id_no VARCHAR(64),
    bank_account VARCHAR(64),
    address VARCHAR(255),
    remark VARCHAR(255),
    created_by BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    name VARCHAR(64) NOT NULL,
    mobile VARCHAR(32) NOT NULL,
    id_no VARCHAR(64),
    gender VARCHAR(16),
    emergency_contact VARCHAR(64),
    emergency_mobile VARCHAR(32),
    remark VARCHAR(255),
    created_by BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE house (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    house_code VARCHAR(64) NOT NULL UNIQUE,
    house_name VARCHAR(128) NOT NULL,
    rental_mode VARCHAR(32) NOT NULL,
    project_name VARCHAR(128) NOT NULL,
    community_name VARCHAR(128),
    address VARCHAR(255) NOT NULL,
    layout_desc VARCHAR(64),
    area DECIMAL(10, 2) NOT NULL,
    rent_price DECIMAL(10, 2) NOT NULL,
    deposit_price DECIMAL(10, 2) DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    landlord_id BIGINT,
    created_by BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE house_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    house_id BIGINT NOT NULL,
    room_code VARCHAR(64) NOT NULL,
    room_name VARCHAR(64) NOT NULL,
    room_area DECIMAL(10, 2),
    rent_price DECIMAL(10, 2),
    status VARCHAR(32) NOT NULL,
    tenant_id BIGINT,
    checkin_date DATE,
    checkout_date DATE
);

CREATE TABLE maintenance_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    house_id BIGINT NOT NULL,
    issue_type VARCHAR(64) NOT NULL,
    description_text VARCHAR(255),
    reporter_name VARCHAR(64),
    assignee_name VARCHAR(64),
    priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE cleaning_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT 1,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    house_id BIGINT NOT NULL,
    cleaning_type VARCHAR(64) NOT NULL,
    appointment_time DATETIME,
    assignee_name VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_ASSIGN',
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
