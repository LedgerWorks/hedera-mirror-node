TRUNCATE TABLE account_balance_sets RESTART IDENTITY CASCADE;
TRUNCATE TABLE account_balance RESTART IDENTITY CASCADE;
TRUNCATE TABLE contract_result RESTART IDENTITY CASCADE;
TRUNCATE TABLE crypto_transfer RESTART IDENTITY CASCADE;
TRUNCATE TABLE file_data RESTART IDENTITY CASCADE;
TRUNCATE TABLE live_hash RESTART IDENTITY CASCADE;
TRUNCATE TABLE record_file RESTART IDENTITY CASCADE;
TRUNCATE TABLE t_entities RESTART IDENTITY CASCADE;
TRUNCATE TABLE transaction RESTART IDENTITY CASCADE;
TRUNCATE TABLE topic_message RESTART IDENTITY CASCADE;
TRUNCATE TABLE non_fee_transfer;
UPDATE t_application_status
SET status_value = NULL;
TRUNCATE TABLE address_book RESTART IDENTITY CASCADE;
TRUNCATE TABLE address_book_entry RESTART IDENTITY CASCADE;
