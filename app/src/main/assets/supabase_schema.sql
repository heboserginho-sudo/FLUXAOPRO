-- ==================================================
-- FLUXAO BUSINESS — SUPABASE MULTI-TENANT SCHEMA (RLS)
-- ==================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. COMPANIES
CREATE TABLE IF NOT EXISTS public.companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    slug TEXT UNIQUE NOT NULL,
    logo_url TEXT DEFAULT '',
    description TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    whatsapp TEXT DEFAULT '',
    email TEXT DEFAULT '',
    address TEXT DEFAULT 'Luanda, Angola',
    city TEXT DEFAULT 'Luanda',
    country TEXT DEFAULT 'AO',
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'suspended')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_companies_slug ON public.companies(slug);

-- 2. COMPANY SETTINGS
CREATE TABLE IF NOT EXISTS public.company_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    currency TEXT NOT NULL DEFAULT 'AOA',
    timezone TEXT NOT NULL DEFAULT 'Africa/Luanda',
    language TEXT NOT NULL DEFAULT 'pt-AO',
    default_tax NUMERIC(5,2) DEFAULT 14.00,
    business_hours TEXT DEFAULT '08:00 - 18:00',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. COMPANY BRANDING
CREATE TABLE IF NOT EXISTS public.company_branding (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    primary_color TEXT DEFAULT '#0F172A',
    secondary_color TEXT DEFAULT '#D4AF37',
    accent_color TEXT DEFAULT '#059669',
    background_color TEXT DEFAULT '#F8FAFC',
    text_color TEXT DEFAULT '#0F172A',
    logo_url TEXT DEFAULT '',
    favicon_url TEXT DEFAULT '',
    app_name TEXT DEFAULT 'FluxAO Business',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. PROFILES
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    company_id UUID REFERENCES public.companies(id) ON DELETE SET NULL,
    full_name TEXT NOT NULL,
    email TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    avatar_url TEXT DEFAULT '',
    role TEXT NOT NULL DEFAULT 'company_admin' CHECK (role IN ('super_admin', 'company_admin', 'employee', 'customer')),
    status TEXT DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. CUSTOMERS
CREATE TABLE IF NOT EXISTS public.customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    profile_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    nif TEXT DEFAULT 'Consumidor Final',
    phone TEXT DEFAULT '',
    whatsapp TEXT DEFAULT '',
    email TEXT DEFAULT '',
    address TEXT DEFAULT '',
    city TEXT DEFAULT 'Luanda',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. EMPLOYEES
CREATE TABLE IF NOT EXISTS public.employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    role_title TEXT DEFAULT 'Atendente',
    permissions JSONB DEFAULT '{"orders": true, "quotes": true, "appointments": true, "customers": true}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. SERVICES
CREATE TABLE IF NOT EXISTS public.services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    price NUMERIC(12,2) DEFAULT 0.00,
    duration_minutes INT DEFAULT 60,
    status TEXT DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. PRODUCTS
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    sku TEXT DEFAULT '',
    category TEXT DEFAULT 'Geral',
    unit_price NUMERIC(12,2) DEFAULT 0.00,
    stock_quantity NUMERIC(10,2) DEFAULT 0.0,
    unit TEXT DEFAULT 'Un',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. ORDERS & ORDER ITEMS
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    order_number TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    total_amount NUMERIC(12,2) DEFAULT 0.00,
    notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    quantity NUMERIC(10,2) DEFAULT 1.0,
    unit_price NUMERIC(12,2) DEFAULT 0.00,
    total_price NUMERIC(12,2) DEFAULT 0.00
);

-- 10. QUOTES & QUOTE ITEMS
CREATE TABLE IF NOT EXISTS public.quotes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    quote_number TEXT NOT NULL,
    project_title TEXT DEFAULT 'Orçamento Comercial',
    client_name TEXT NOT NULL,
    total_amount NUMERIC(12,2) DEFAULT 0.00,
    status TEXT DEFAULT 'RASCUNHO',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.quote_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quote_id UUID NOT NULL REFERENCES public.quotes(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    quantity NUMERIC(10,2) DEFAULT 1.0,
    unit_price NUMERIC(12,2) DEFAULT 0.00,
    total_price NUMERIC(12,2) DEFAULT 0.00
);

-- 11. APPOINTMENTS
CREATE TABLE IF NOT EXISTS public.appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    client_name TEXT NOT NULL,
    client_phone TEXT DEFAULT '',
    service_type TEXT DEFAULT 'Geral',
    appointment_date TIMESTAMPTZ NOT NULL,
    time_slot TEXT DEFAULT '09:00',
    status TEXT DEFAULT 'AGENDADO',
    reminder_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 12. PAYMENTS
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    order_id UUID REFERENCES public.orders(id) ON DELETE SET NULL,
    amount NUMERIC(12,2) NOT NULL,
    payment_method TEXT DEFAULT 'MULTICAIXA_EXPRESS',
    status TEXT DEFAULT 'CONCLUIDO',
    reference_number TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 13. NOTIFICATIONS
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 14. PLANS & SUBSCRIPTIONS
CREATE TABLE IF NOT EXISTS public.plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    price_aoa NUMERIC(12,2) NOT NULL,
    features JSONB DEFAULT '[]'::jsonb
);

CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    plan_id UUID REFERENCES public.plans(id) ON DELETE SET NULL,
    status TEXT DEFAULT 'active',
    starts_at TIMESTAMPTZ DEFAULT NOW(),
    ends_at TIMESTAMPTZ DEFAULT NOW() + INTERVAL '30 days'
);

-- ==================================================
-- ROW LEVEL SECURITY (RLS) & HELPER FUNCTIONS
-- ==================================================

-- Secure function to discover the user's company_id from profiles
CREATE OR REPLACE FUNCTION public.get_user_company_id()
RETURNS UUID AS $$
BEGIN
    RETURN (
        SELECT company_id
        FROM public.profiles
        WHERE id = auth.uid()
        LIMIT 1
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Secure function to check if the current user is a super_admin
CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM public.profiles
        WHERE id = auth.uid() AND role = 'super_admin'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable RLS on all tenant tables
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.company_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.company_branding ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.services ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quote_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;

-- 1. COMPANIES POLICIES
CREATE POLICY "Super Admins can access all companies" ON public.companies
    FOR ALL USING (public.is_super_admin());

CREATE POLICY "Company Admins can view own company" ON public.companies
    FOR SELECT USING (id = public.get_user_company_id());

CREATE POLICY "Company Admins can update own company" ON public.companies
    FOR UPDATE USING (id = public.get_user_company_id());

-- 2. PROFILES POLICIES
CREATE POLICY "Users can view own profile or company profiles" ON public.profiles
    FOR SELECT USING (
        public.is_super_admin() OR id = auth.uid() OR company_id = public.get_user_company_id()
    );

CREATE POLICY "Users can insert own profile" ON public.profiles
    FOR INSERT WITH CHECK (id = auth.uid() OR public.is_super_admin());

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (id = auth.uid() OR public.is_super_admin());

-- 3. MULTI-TENANT GENERIC ISOLATION POLICY CREATION
-- Applies strict company_id isolation for: customers, employees, services, products, orders, quotes, appointments, payments, notifications, company_settings, company_branding

CREATE POLICY "Tenant isolation select" ON public.customers FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.customers FOR INSERT WITH CHECK (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.customers FOR UPDATE USING (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation delete" ON public.customers FOR DELETE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.services FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.services FOR INSERT WITH CHECK (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.services FOR UPDATE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.products FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.products FOR INSERT WITH CHECK (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.products FOR UPDATE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.orders FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.orders FOR INSERT WITH CHECK (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.orders FOR UPDATE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.quotes FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.quotes FOR INSERT WITH CHECK (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.appointments FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.appointments FOR INSERT WITH CHECK (company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.appointments FOR UPDATE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.payments FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.payments FOR INSERT WITH CHECK (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.notifications FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation insert" ON public.notifications FOR INSERT WITH CHECK (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.company_settings FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.company_settings FOR UPDATE USING (company_id = public.get_user_company_id());

CREATE POLICY "Tenant isolation select" ON public.company_branding FOR SELECT USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Tenant isolation update" ON public.company_branding FOR UPDATE USING (company_id = public.get_user_company_id());

-- ==================================================
225. GAMIFICATION & REWARDS SYSTEM (FLUXAO POINTS)
-- ==================================================

-- 15. USER POINTS (Wallet / Balance)
CREATE TABLE IF NOT EXISTS public.user_points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
    lifetime_earned BIGINT NOT NULL DEFAULT 0 CHECK (lifetime_earned >= 0),
    lifetime_spent BIGINT NOT NULL DEFAULT 0 CHECK (lifetime_spent >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    CONSTRAINT uq_user_points_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_points_user_id ON public.user_points(user_id);

-- 16. POINTS TRANSACTIONS (Audit Ledger)
CREATE TABLE IF NOT EXISTS public.points_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('earn', 'spend', 'adjustment', 'reversal')),
    source TEXT NOT NULL CHECK (source IN (
        'rewarded_ad',
        'invoice_created',
        'invoice_whatsapp',
        'quote_created',
        'customer_created',
        'appointment_created',
        'admin_adjustment',
        'reward_redemption'
    )),
    points INT NOT NULL,
    reference_id TEXT DEFAULT '',
    description TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'completed' CHECK (status IN ('completed', 'pending', 'cancelled', 'reversed')),
    ip_address TEXT DEFAULT '',
    user_agent TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

-- Idempotency Index: Prevents earning multiple times for the exact same action reference (e.g. same invoice UUID)
CREATE UNIQUE INDEX IF NOT EXISTS uq_points_transactions_idempotency 
    ON public.points_transactions(user_id, source, reference_id) 
    WHERE reference_id <> '' AND type = 'earn';

CREATE INDEX IF NOT EXISTS idx_points_transactions_user_id ON public.points_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_points_transactions_created_at ON public.points_transactions(created_at DESC);

-- 17. POINT RULES (Dynamic Admin Configuration)
CREATE TABLE IF NOT EXISTS public.point_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    action TEXT UNIQUE NOT NULL,
    points INT NOT NULL DEFAULT 0 CHECK (points >= 0),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    daily_limit INT DEFAULT NULL,
    requires_validation BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

-- Seed Initial Rules
INSERT INTO public.point_rules (action, points, enabled, daily_limit, requires_validation) VALUES
('rewarded_ad', 10, true, 5, true),
('invoice_created', 2, true, NULL, false),
('invoice_whatsapp', 2, true, NULL, false),
('quote_created', 2, true, NULL, false),
('customer_created', 2, true, NULL, false),
('appointment_created', 2, true, NULL, false)
ON CONFLICT (action) DO NOTHING;

-- 18. REWARDS CATALOG (Global & Country-Specific)
CREATE TABLE IF NOT EXISTS public.rewards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    points_required INT NOT NULL CHECK (points_required > 0),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    stock INT DEFAULT -1, -- -1 = unlimited
    country TEXT DEFAULT 'GLOBAL',
    currency TEXT DEFAULT 'ALL',
    expires_at TIMESTAMPTZ DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

-- 19. POINT AUDIT LOGS (Anti-Fraud Tracking)
CREATE TABLE IF NOT EXISTS public.point_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL, -- e.g. 'SUSPICIOUS_RAPID_CLICK', 'DUPLICATE_ATTEMPT', 'DAILY_LIMIT_EXCEEDED'
    details JSONB DEFAULT '{}'::jsonb,
    ip_address TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable RLS for Points & Rewards Tables
ALTER TABLE public.user_points ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.points_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.point_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rewards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.point_audit_logs ENABLE ROW LEVEL SECURITY;

-- POLICIES: USER POINTS
CREATE POLICY "Users can view own points balance" ON public.user_points
    FOR SELECT USING (auth.uid() = user_id OR public.is_super_admin());

-- POLICIES: POINTS TRANSACTIONS
CREATE POLICY "Users can view own points transactions" ON public.points_transactions
    FOR SELECT USING (auth.uid() = user_id OR public.is_super_admin());

-- POLICIES: POINT RULES
CREATE POLICY "Everyone can read point rules" ON public.point_rules
    FOR SELECT USING (true);

CREATE POLICY "Super Admins can modify point rules" ON public.point_rules
    FOR ALL USING (public.is_super_admin());

-- POLICIES: REWARDS
CREATE POLICY "Everyone can read active rewards" ON public.rewards
    FOR SELECT USING (enabled = true OR public.is_super_admin());

CREATE POLICY "Super Admins can modify rewards" ON public.rewards
    FOR ALL USING (public.is_super_admin());

-- POLICIES: AUDIT LOGS
CREATE POLICY "Super Admins can view audit logs" ON public.point_audit_logs
    FOR SELECT USING (public.is_super_admin());

-- ATOMIC STORED PROCEDURE: AWARD USER POINTS (Anti-Fraud & Idempotent)
CREATE OR REPLACE FUNCTION public.award_user_points(
    p_user_id UUID,
    p_source TEXT,
    p_reference_id TEXT DEFAULT '',
    p_custom_desc TEXT DEFAULT NULL,
    p_ip_address TEXT DEFAULT ''
)
RETURNS JSONB AS $$
DECLARE
    v_rule RECORD;
    v_points INT;
    v_desc TEXT;
    v_existing_count INT;
    v_daily_count INT;
    v_current_balance BIGINT;
    v_new_balance BIGINT;
    v_transaction_id UUID;
BEGIN
    -- 1. Fetch active rule
    SELECT * INTO v_rule FROM public.point_rules WHERE action = p_source AND enabled = true;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'reason', 'Rule disabled or not found');
    END IF;

    v_points := v_rule.points;
    v_desc := COALESCE(p_custom_desc, 'Recompensa por ação: ' || p_source);

    -- 2. Check Idempotency (if reference_id is provided)
    IF p_reference_id IS NOT NULL AND p_reference_id <> '' THEN
        SELECT COUNT(*) INTO v_existing_count 
        FROM public.points_transactions 
        WHERE user_id = p_user_id AND source = p_source AND reference_id = p_reference_id AND type = 'earn';

        IF v_existing_count > 0 THEN
            RETURN jsonb_build_object('success', false, 'reason', 'ALREADY_AWARDED', 'message', 'Pontos já concedidos para esta referência');
        END IF;
    END IF;

    -- 3. Check Daily Limit
    IF v_rule.daily_limit IS NOT NULL THEN
        SELECT COUNT(*) INTO v_daily_count
        FROM public.points_transactions
        WHERE user_id = p_user_id AND source = p_source AND created_at >= CURRENT_DATE AND type = 'earn';

        IF v_daily_count >= v_rule.daily_limit THEN
            INSERT INTO public.point_audit_logs (user_id, event_type, details, ip_address)
            VALUES (p_user_id, 'DAILY_LIMIT_EXCEEDED', jsonb_build_object('source', p_source, 'limit', v_rule.daily_limit), p_ip_address);

            RETURN jsonb_build_object('success', false, 'reason', 'DAILY_LIMIT_EXCEEDED', 'message', 'Limite diário atingido para esta ação');
        END IF;
    END IF;

    -- 4. Upsert User Points Lock & Update Balance
    INSERT INTO public.user_points (user_id, balance, lifetime_earned, created_at, updated_at)
    VALUES (p_user_id, v_points, v_points, NOW(), NOW())
    ON CONFLICT (user_id) DO UPDATE SET
        balance = user_points.balance + EXCLUDED.balance,
        lifetime_earned = user_points.lifetime_earned + EXCLUDED.lifetime_earned,
        updated_at = NOW()
    RETURNING balance INTO v_new_balance;

    -- 5. Record Transaction Ledger
    INSERT INTO public.points_transactions (user_id, type, source, points, reference_id, description, status, ip_address, created_at)
    VALUES (p_user_id, 'earn', p_source, v_points, COALESCE(p_reference_id, ''), v_desc, 'completed', p_ip_address, NOW())
    RETURNING id INTO v_transaction_id;

    RETURN jsonb_build_object(
        'success', true,
        'points_awarded', v_points,
        'new_balance', v_new_balance,
        'transaction_id', v_transaction_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- ==================================================
-- 226. INTERNATIONAL MULTI-COUNTRY FISCAL INVOICING
-- ==================================================

-- 20. COUNTRY FISCAL CONFIGURATIONS
CREATE TABLE IF NOT EXISTS public.country_fiscal_configs (
    country_code VARCHAR(5) PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL,
    tax_authority_name VARCHAR(100) NOT NULL,
    default_currency VARCHAR(10) NOT NULL DEFAULT 'AOA',
    tax_id_label VARCHAR(50) NOT NULL DEFAULT 'NIF',
    default_provider_id VARCHAR(100) NOT NULL DEFAULT 'AngolaAGTProvider',
    require_fiscal_validation BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed Default Countries
INSERT INTO public.country_fiscal_configs (country_code, country_name, tax_authority_name, default_currency, tax_id_label, default_provider_id, require_fiscal_validation) VALUES
('AO', 'Angola', 'AGT', 'AOA', 'NIF', 'AngolaAGTProvider', true),
('PT', 'Portugal', 'AT', 'EUR', 'NIF / NIPC', 'PortugalATProvider', true),
('BR', 'Brasil', 'SEFAZ / Receita Federal', 'BRL', 'CNPJ / CPF', 'BrazilFiscalProvider', true),
('FR', 'França', 'DGFiP', 'EUR', 'SIRET / TVA Intracommunautaire', 'FranceFiscalProvider', false),
('ES', 'Espanha', 'AEAT / Veri*Factu', 'EUR', 'NIF / NIE', 'SpainFiscalProvider', true),
('US', 'Estados Unidos', 'IRS', 'USD', 'EIN / SSN', 'GenericInvoiceProvider_US', false)
ON CONFLICT (country_code) DO NOTHING;

-- 21. BUSINESS FISCAL PROFILES
CREATE TABLE IF NOT EXISTS public.business_fiscal_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id TEXT NOT NULL UNIQUE,
    legal_name TEXT NOT NULL,
    trade_name TEXT DEFAULT '',
    country_code VARCHAR(5) NOT NULL DEFAULT 'AO' REFERENCES public.country_fiscal_configs(country_code),
    tax_id TEXT NOT NULL,
    tax_id_type VARCHAR(50) NOT NULL DEFAULT 'NIF',
    fiscal_provider_id VARCHAR(100) NOT NULL DEFAULT 'AngolaAGTProvider',
    invoice_series VARCHAR(20) DEFAULT 'AO2026',
    address TEXT DEFAULT '',
    city TEXT DEFAULT '',
    state TEXT DEFAULT '',
    postal_code TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    email TEXT DEFAULT '',
    currency VARCHAR(10) NOT NULL DEFAULT 'AOA',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 22. FISCAL AUDIT LOGS
CREATE TABLE IF NOT EXISTS public.fiscal_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    invoice_id TEXT DEFAULT '',
    action VARCHAR(50) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
    request_reference TEXT DEFAULT '',
    response_reference TEXT DEFAULT '',
    details TEXT DEFAULT '',
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable RLS for International Fiscal Tables
ALTER TABLE public.country_fiscal_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.business_fiscal_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fiscal_audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view country fiscal configs" ON public.country_fiscal_configs FOR SELECT USING (true);
CREATE POLICY "Tenant isolation business fiscal profiles" ON public.business_fiscal_profiles FOR ALL USING (public.is_super_admin() OR company_id = public.get_user_company_id());
CREATE POLICY "Super Admins view fiscal audit logs" ON public.fiscal_audit_logs FOR SELECT USING (public.is_super_admin());

-- ==================================================
-- 227. RECORD POINTS EVENT RPC (Absoluto: Apenas Ad = +10, Ações = -2)
-- ==================================================
CREATE OR REPLACE FUNCTION public.record_points_event(
    p_user_id UUID,
    p_source TEXT,
    p_reference_id TEXT DEFAULT '',
    p_description TEXT DEFAULT NULL,
    p_metadata JSONB DEFAULT '{}'::jsonb
)
RETURNS JSONB AS $$
DECLARE
    v_points_change INT;
    v_current_balance BIGINT;
    v_new_balance BIGINT;
    v_transaction_id UUID;
    v_existing_count INT;
BEGIN
    -- 1. Enforce Points Delta: ONLY rewarded_ad grants +10. All business actions deduct -2.
    IF p_source = 'rewarded_ad' THEN
        v_points_change := 10;
    ELSIF p_source IN (
        'invoice_created',
        'invoice_whatsapp',
        'invoice_downloaded',
        'customer_created',
        'quote_created',
        'fluxai_consultation',
        'movement_created',
        'appointment_created'
    ) THEN
        v_points_change := -2;
    ELSE
        RETURN jsonb_build_object('success', false, 'reason', 'UNAUTHORIZED_SOURCE', 'message', 'Fonte de pontos não autorizada');
    END IF;

    -- 2. Idempotency Check: Prevent double-deduct or double-grant for same reference_id
    IF p_reference_id IS NOT NULL AND p_reference_id <> '' THEN
        SELECT COUNT(*) INTO v_existing_count
        FROM public.points_transactions
        WHERE user_id = p_user_id AND source = p_source AND reference_id = p_reference_id;

        IF v_existing_count > 0 THEN
            SELECT balance INTO v_current_balance FROM public.user_points WHERE user_id = p_user_id;
            RETURN jsonb_build_object('success', true, 'reason', 'ALREADY_PROCESSED', 'balance', COALESCE(v_current_balance, 0));
        END IF;
    END IF;

    -- 3. Get or Lock User Points
    SELECT balance INTO v_current_balance FROM public.user_points WHERE user_id = p_user_id FOR UPDATE;
    IF v_current_balance IS NULL THEN
        v_current_balance := 0;
    END IF;

    -- 4. Check Sufficiency if Deducting
    IF v_points_change < 0 AND v_current_balance < ABS(v_points_change) THEN
        RETURN jsonb_build_object(
            'success', false,
            'reason', 'INSUFFICIENT_POINTS',
            'current_balance', v_current_balance,
            'required_points', ABS(v_points_change)
        );
    END IF;

    -- 5. Calculate New Balance
    v_new_balance := v_current_balance + v_points_change;

    -- 6. Upsert Wallet
    INSERT INTO public.user_points (user_id, balance, lifetime_earned, lifetime_spent, created_at, updated_at)
    VALUES (
        p_user_id,
        v_new_balance,
        CASE WHEN v_points_change > 0 THEN v_points_change ELSE 0 END,
        CASE WHEN v_points_change < 0 THEN ABS(v_points_change) ELSE 0 END,
        NOW(), NOW()
    )
    ON CONFLICT (user_id) DO UPDATE SET
        balance = v_new_balance,
        lifetime_earned = user_points.lifetime_earned + (CASE WHEN v_points_change > 0 THEN v_points_change ELSE 0 END),
        lifetime_spent = user_points.lifetime_spent + (CASE WHEN v_points_change < 0 THEN ABS(v_points_change) ELSE 0 END),
        updated_at = NOW();

    -- 7. Record Transaction Ledger
    INSERT INTO public.points_transactions (user_id, type, source, points, reference_id, description, status, created_at)
    VALUES (
        p_user_id,
        CASE WHEN v_points_change > 0 THEN 'earn' ELSE 'spend' END,
        p_source,
        v_points_change,
        COALESCE(p_reference_id, ''),
        COALESCE(p_description, p_source),
        'completed',
        NOW()
    )
    RETURNING id INTO v_transaction_id;

    RETURN jsonb_build_object(
        'success', true,
        'points_changed', v_points_change,
        'new_balance', v_new_balance,
        'transaction_id', v_transaction_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- ==================================================
-- 228. POINT REWARDS CATALOG, REDEMPTIONS & ENTITLEMENTS
-- ==================================================

-- 23. POINT REWARDS TABLE
CREATE TABLE IF NOT EXISTS public.point_rewards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    icon TEXT DEFAULT 'stars',
    points_cost INT NOT NULL CHECK (points_cost > 0),
    duration_days INT DEFAULT 30, -- NULL for permanent
    category TEXT DEFAULT 'feature_unlock', -- feature_unlock, template, discount, support
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    stock INT DEFAULT -1, -- -1 = unlimited
    country TEXT DEFAULT 'GLOBAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_point_rewards_code ON public.point_rewards(code);
CREATE INDEX IF NOT EXISTS idx_point_rewards_enabled ON public.point_rewards(enabled);
CREATE INDEX IF NOT EXISTS idx_point_rewards_category ON public.point_rewards(category);

-- Seed Reward Catalog
INSERT INTO public.point_rewards (id, code, name, description, points_cost, duration_days, category, icon, enabled, stock) VALUES
('11111111-1111-1111-1111-111111111101', 'reports_advanced', 'Relatórios Avançados', 'Desbloqueie relatórios financeiros avançados, DRE, análises e exportações em PDF.', 50, 30, 'feature_unlock', 'Assessment', true, -1),
('11111111-1111-1111-1111-111111111102', 'invoice_premium', 'Template de Fatura Premium', 'Desbloqueie layouts premium de faturas, logotipo, cores e personalização avançada.', 80, 30, 'template', 'Palette', true, -1),
('11111111-1111-1111-1111-111111111103', 'subscription_discount', 'Desconto na Subscrição', 'Desbloqueie um desconto de 20% no próximo ciclo de faturação.', 120, 30, 'discount', 'LocalOffer', true, -1),
('11111111-1111-1111-1111-111111111104', 'whatsapp_automation', 'Automação WhatsApp', 'Desbloqueie automações avançadas de envio e lembretes por WhatsApp.', 150, 30, 'feature_unlock', 'Chat', true, -1),
('11111111-1111-1111-1111-111111111105', 'vip_support', 'Suporte Prioritário VIP', 'Acesso direto a consultor dedicado no WhatsApp com resposta em 15 minutos.', 200, 30, 'support', 'SupportAgent', true, -1),
('11111111-1111-1111-1111-111111111106', 'pro_plan', 'Plano Pro Completo', 'Desbloqueie o Plano Pro com acesso total a todas as funcionalidades Globais e Multipaís.', 300, 30, 'discount', 'WorkspacePremium', true, -1)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    points_cost = EXCLUDED.points_cost,
    duration_days = EXCLUDED.duration_days,
    category = EXCLUDED.category,
    icon = EXCLUDED.icon;

-- 24. POINT REDEMPTIONS TABLE (Audit Ledger of Redeemed Rewards)
CREATE TABLE IF NOT EXISTS public.point_redemptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reward_id UUID NOT NULL REFERENCES public.point_rewards(id) ON DELETE RESTRICT,
    reward_code TEXT NOT NULL,
    points_spent INT NOT NULL CHECK (points_spent > 0),
    status TEXT NOT NULL DEFAULT 'completed' CHECK (status IN ('completed', 'pending', 'cancelled', 'refunded')),
    reference_id TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_point_redemptions_user_id ON public.point_redemptions(user_id);
CREATE INDEX IF NOT EXISTS idx_point_redemptions_reward_id ON public.point_redemptions(reward_id);
CREATE INDEX IF NOT EXISTS idx_point_redemptions_created_at ON public.point_redemptions(created_at DESC);

-- 25. USER ENTITLEMENTS TABLE (Active Features & Privileges Granted to User)
CREATE TABLE IF NOT EXISTS public.user_entitlements (
    id TEXT PRIMARY KEY, -- e.g. 'reports_advanced_user123_1710000000'
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reward_id UUID REFERENCES public.point_rewards(id) ON DELETE SET NULL,
    reward_code TEXT NOT NULL,
    reward_title TEXT NOT NULL,
    redemption_id UUID REFERENCES public.point_redemptions(id) ON DELETE SET NULL,
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'expired', 'revoked', 'cancelled')),
    activated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT NULL, -- NULL = permanent
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_entitlements_user_id ON public.user_entitlements(user_id);
CREATE INDEX IF NOT EXISTS idx_user_entitlements_code ON public.user_entitlements(user_id, reward_code);
CREATE INDEX IF NOT EXISTS idx_user_entitlements_status ON public.user_entitlements(status, expires_at);

-- ROW LEVEL SECURITY FOR REWARDS, REDEMPTIONS & ENTITLEMENTS
ALTER TABLE public.point_rewards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.point_redemptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_entitlements ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can view active point rewards" ON public.point_rewards
    FOR SELECT USING (enabled = true OR public.is_super_admin());

CREATE POLICY "Super Admins can manage point rewards" ON public.point_rewards
    FOR ALL USING (public.is_super_admin());

CREATE POLICY "Users can view own point redemptions" ON public.point_redemptions
    FOR SELECT USING (auth.uid() = user_id OR public.is_super_admin());

CREATE POLICY "Users can insert own point redemptions" ON public.point_redemptions
    FOR INSERT WITH CHECK (auth.uid() = user_id OR public.is_super_admin());

CREATE POLICY "Users can view own user entitlements" ON public.user_entitlements
    FOR SELECT USING (auth.uid() = user_id OR public.is_super_admin());

CREATE POLICY "Users can insert/update own user entitlements" ON public.user_entitlements
    FOR ALL USING (auth.uid() = user_id OR public.is_super_admin());


-- ATOMIC STORED PROCEDURE: REDEEM POINT REWARD (Deducts points, creates redemption, grants entitlement)
CREATE OR REPLACE FUNCTION public.redeem_point_reward(
    p_user_id UUID,
    p_reward_code TEXT,
    p_reference_id TEXT DEFAULT ''
)
RETURNS JSONB AS $$
DECLARE
    v_reward RECORD;
    v_current_balance BIGINT;
    v_new_balance BIGINT;
    v_redemption_id UUID;
    v_entitlement_id TEXT;
    v_expires_at TIMESTAMPTZ;
    v_now TIMESTAMPTZ := NOW();
    v_active_count INT;
BEGIN
    -- 1. Fetch reward definition
    SELECT * INTO v_reward FROM public.point_rewards WHERE code = p_reward_code AND enabled = true;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'reason', 'REWARD_NOT_FOUND', 'message', 'Recompensa indisponível ou não encontrada');
    END IF;

    -- 2. Check stock
    IF v_reward.stock = 0 THEN
        RETURN jsonb_build_object('success', false, 'reason', 'OUT_OF_STOCK', 'message', 'Estoque esgotado para esta recompensa');
    END IF;

    -- 3. Check if user already has an active entitlement for this reward_code
    SELECT COUNT(*) INTO v_active_count
    FROM public.user_entitlements
    WHERE user_id = p_user_id AND reward_code = p_reward_code AND status = 'active'
      AND (expires_at IS NULL OR expires_at > v_now);

    IF v_active_count > 0 THEN
        RETURN jsonb_build_object('success', false, 'reason', 'ALREADY_ACTIVE', 'message', 'Você já possui este benefício ativo na sua conta!');
    END IF;

    -- 4. Check user points balance with lock
    SELECT balance INTO v_current_balance FROM public.user_points WHERE user_id = p_user_id FOR UPDATE;
    IF v_current_balance IS NULL OR v_current_balance < v_reward.points_cost THEN
        RETURN jsonb_build_object(
            'success', false,
            'reason', 'INSUFFICIENT_POINTS',
            'message', 'Pontos insuficientes para resgatar este benefício',
            'current_balance', COALESCE(v_current_balance, 0),
            'required_points', v_reward.points_cost
        );
    END IF;

    -- 5. Calculate & Update balance
    v_new_balance := v_current_balance - v_reward.points_cost;

    UPDATE public.user_points
    SET balance = v_new_balance,
        lifetime_spent = lifetime_spent + v_reward.points_cost,
        updated_at = v_now
    WHERE user_id = p_user_id;

    -- 6. Record points transaction ledger
    INSERT INTO public.points_transactions (
        user_id, type, source, points, reference_id, description, status, created_at
    ) VALUES (
        p_user_id, 'spend', 'reward_redemption', -v_reward.points_cost,
        v_reward.id::text, 'Resgate de recompensa: ' || v_reward.name, 'completed', v_now
    );

    -- 7. Record point redemption
    INSERT INTO public.point_redemptions (
        user_id, reward_id, reward_code, points_spent, status, reference_id, created_at
    ) VALUES (
        p_user_id, v_reward.id, v_reward.code, v_reward.points_cost, 'completed', p_reference_id, v_now
    ) RETURNING id INTO v_redemption_id;

    -- 8. Calculate expiration date
    IF v_reward.duration_days IS NOT NULL AND v_reward.duration_days > 0 THEN
        v_expires_at := v_now + (v_reward.duration_days || ' days')::INTERVAL;
    ELSE
        v_expires_at := NULL;
    END IF;

    -- 9. Insert entitlement
    v_entitlement_id := v_reward.code || '_' || p_user_id::text || '_' || EXTRACT(EPOCH FROM v_now)::BIGINT;

    INSERT INTO public.user_entitlements (
        id, user_id, reward_id, reward_code, reward_title, redemption_id, status, activated_at, expires_at, created_at, updated_at
    ) VALUES (
        v_entitlement_id, p_user_id, v_reward.id, v_reward.code, v_reward.name, v_redemption_id, 'active', v_now, v_expires_at, v_now, v_now
    );

    -- 10. Decrement stock if finite
    IF v_reward.stock > 0 THEN
        UPDATE public.point_rewards
        SET stock = stock - 1, updated_at = v_now
        WHERE id = v_reward.id;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'new_balance', v_new_balance,
        'redemption_id', v_redemption_id,
        'entitlement_id', v_entitlement_id,
        'reward_code', v_reward.code,
        'expires_at', v_expires_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- ==================================================
-- 229. APPLICATION FEATURES CATALOG (Access Control Model)
-- ==================================================

CREATE TABLE IF NOT EXISTS public.features (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    access_type TEXT NOT NULL DEFAULT 'POINT_UNLOCK' CHECK (access_type IN ('FREE_FEATURE', 'POINT_UNLOCK', 'SUBSCRIPTION')),
    points_cost INT NOT NULL DEFAULT 0 CHECK (points_cost >= 0),
    duration_days INT DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed Application Features
INSERT INTO public.features (code, name, description, access_type, points_cost, duration_days) VALUES
('cashflow_basic', 'Fluxo de Caixa', 'Gestão básica de receitas, despesas e saldo do fluxo de caixa.', 'FREE_FEATURE', 0, NULL),
('invoices', 'Faturas', 'Emissão e gestão de faturas comerciais e fiscais.', 'POINT_UNLOCK', 50, 30),
('customers', 'Clientes', 'Gestão completa da carteira de clientes e dados fiscais.', 'POINT_UNLOCK', 30, 30),
('appointments', 'Agendamentos', 'Gestão de agenda, compromissos e marcações.', 'POINT_UNLOCK', 30, 30),
('reports', 'Relatórios', 'Relatórios financeiros avançados, DRE e análises.', 'POINT_UNLOCK', 50, 30),
('fluxoai', 'FluxoAI', 'Consultoria e assistente inteligente para finanças e negócios.', 'POINT_UNLOCK', 80, 30),
('whatsapp_automation', 'WhatsApp Avançado', 'Envio automático de lembretes e faturas via WhatsApp.', 'POINT_UNLOCK', 150, 30)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    access_type = EXCLUDED.access_type,
    points_cost = EXCLUDED.points_cost,
    duration_days = EXCLUDED.duration_days;

-- Sync Seed in public.point_rewards
INSERT INTO public.point_rewards (id, code, name, description, points_cost, duration_days, category, icon, enabled, stock) VALUES
('11111111-1111-1111-1111-111111111110', 'invoices', 'Faturas', 'Emita e gerencie suas faturas comerciais e fiscais.', 50, 30, 'feature_unlock', 'ReceiptLong', true, -1),
('11111111-1111-1111-1111-111111111111', 'customers', 'Clientes', 'Gerencie sua carteira de clientes e contatos.', 30, 30, 'feature_unlock', 'People', true, -1),
('11111111-1111-1111-1111-111111111112', 'appointments', 'Agendamentos', 'Gerencie seus compromissos e agenda comercial.', 30, 30, 'feature_unlock', 'CalendarMonth', true, -1),
('11111111-1111-1111-1111-111111111113', 'reports', 'Relatórios', 'Relatórios financeiros avançados, DRE e estatísticas.', 50, 30, 'feature_unlock', 'Assessment', true, -1),
('11111111-1111-1111-1111-111111111114', 'fluxoai', 'FluxoAI', 'Consultas ilimitadas ao assistente financeiro IA.', 80, 30, 'feature_unlock', 'AutoAwesome', true, -1),
('11111111-1111-1111-1111-111111111115', 'whatsapp_automation', 'Automação WhatsApp', 'Desbloqueie automações avançadas de envio e lembretes por WhatsApp.', 150, 30, 'feature_unlock', 'Chat', true, -1)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    points_cost = EXCLUDED.points_cost,
    duration_days = EXCLUDED.duration_days,
    category = EXCLUDED.category,
    icon = EXCLUDED.icon;

ALTER TABLE public.features ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Everyone can read active features" ON public.features
    FOR SELECT USING (is_active = true OR public.is_super_admin());

CREATE POLICY "Super Admins can modify features" ON public.features
    FOR ALL USING (public.is_super_admin());

-- ==================================================
-- 230. USER FEATURE UNLOCKS (Persistent Module Unlocking)
-- ==================================================
CREATE TABLE IF NOT EXISTS public.user_feature_unlocks (
    id TEXT PRIMARY KEY DEFAULT ('unlock_' || uuid_generate_v4()::text),
    user_id TEXT NOT NULL,
    feature_key TEXT NOT NULL,
    unlocked BOOLEAN NOT NULL DEFAULT TRUE,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points_spent INT NOT NULL DEFAULT 0 CHECK (points_spent >= 0),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_feature_unlock UNIQUE (user_id, feature_key)
);

CREATE INDEX IF NOT EXISTS idx_user_feature_unlocks_user_key ON public.user_feature_unlocks(user_id, feature_key);
CREATE INDEX IF NOT EXISTS idx_user_feature_unlocks_active ON public.user_feature_unlocks(user_id, unlocked);

ALTER TABLE public.user_feature_unlocks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their unlocked features" ON public.user_feature_unlocks
    FOR SELECT USING (user_id = auth.uid()::text OR user_id = 'default_user' OR public.is_super_admin());

CREATE POLICY "Users can unlock features for themselves" ON public.user_feature_unlocks
    FOR INSERT WITH CHECK (user_id = auth.uid()::text OR user_id = 'default_user' OR public.is_super_admin());

CREATE POLICY "Users can update their feature unlock status" ON public.user_feature_unlocks
    FOR UPDATE USING (user_id = auth.uid()::text OR user_id = 'default_user' OR public.is_super_admin());

-- Function: check_feature_unlock(p_user_id, p_feature_key)
CREATE OR REPLACE FUNCTION public.check_feature_unlock(
    p_user_id TEXT,
    p_feature_key TEXT
)
RETURNS BOOLEAN AS $$
DECLARE
    v_unlocked BOOLEAN;
    v_canonical TEXT;
BEGIN
    -- Normalize aliases
    v_canonical := lower(trim(p_feature_key));
    IF v_canonical IN ('cashflow', 'cashflow_basic', 'fluxo_caixa') THEN
        RETURN TRUE;
    END IF;

    IF v_canonical IN ('agenda', 'appointments', 'agendamentos') THEN
        v_canonical := 'agenda';
    ELSIF v_canonical IN ('invoices', 'faturas', 'invoice') THEN
        v_canonical := 'invoices';
    ELSIF v_canonical IN ('clients', 'customers', 'clientes') THEN
        v_canonical := 'clients';
    ELSIF v_canonical IN ('reports', 'advanced_reports', 'relatorios') THEN
        v_canonical := 'advanced_reports';
    ELSIF v_canonical IN ('fluxoai', 'ai', 'ia') THEN
        v_canonical := 'fluxoai';
    END IF;

    SELECT (unlocked AND (expires_at IS NULL OR expires_at > NOW())) INTO v_unlocked
    FROM public.user_feature_unlocks
    WHERE user_id = p_user_id 
      AND (feature_key = v_canonical OR feature_key = p_feature_key)
    LIMIT 1;

    RETURN COALESCE(v_unlocked, FALSE);
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;




