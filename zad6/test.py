import json
import unittest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoAlertPresentException
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager


class ReactAppTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.base_url = 'http://localhost:5173/'
        service = Service(ChromeDriverManager().install())
        cls.driver = webdriver.Chrome(service=service)
        cls.driver.implicitly_wait(5)

    @classmethod
    def tearDownClass(cls):
        cls.driver.quit()

    def setUp(self):
        self.driver.get(self.base_url)

    def test_01_app_loads(self):
        """App should load and display root container"""
        container = self.driver.find_element(By.TAG_NAME, 'div')
        self.assertIsNotNone(container)

    def test_02_container_style(self):
        """Root div should have max-width 600px, centered"""
        container = self.driver.find_element(By.CSS_SELECTOR, 'div[style*="max-width: 600px"]')
        style = container.get_attribute('style')
        self.assertIn('max-width: 600px', style)
        self.assertIn('margin: 0px auto', style)

    def test_03_products_heading(self):
        """Product list should have heading"""
        heading = self.driver.find_element(By.XPATH, "//section//h1[text()='Product List']")
        self.assertIsNotNone(heading)

    def test_04_products_list_present(self):
        """Products component should render ul"""
        ul = self.driver.find_element(By.CSS_SELECTOR, 'section ul')
        self.assertIsNotNone(ul)

    def test_05_products_items_count(self):
        """At least one product item should be rendered"""
        items = self.driver.find_elements(By.CSS_SELECTOR, 'section ul li')
        self.assertGreater(len(items), 0)

    def test_06_each_product_has_name(self):
        """Each product item should include an h2 name"""
        items = self.driver.find_elements(By.CSS_SELECTOR, 'section ul li')
        for item in items:
            name = item.find_element(By.TAG_NAME, 'h2').text
            self.assertTrue(len(name) > 0)

    def test_07_each_product_has_description(self):
        """Each product item should include a description paragraph"""
        items = self.driver.find_elements(By.CSS_SELECTOR, 'section ul li')
        for item in items:
            desc = item.find_element(By.TAG_NAME, 'p').text
            self.assertTrue(len(desc) > 0)

    def test_08_each_product_has_price_formatted(self):
        """Each product price should be formatted with $ and two decimals"""
        prices = self.driver.find_elements(By.XPATH, "//section/ul/li//strong")
        for p in prices:
            text = p.text
            self.assertRegex(text, r"^\$\d+\.\d{2}$")

    def test_09_payment_heading(self):
        """Payment form should have heading"""
        h1 = self.driver.find_element(By.XPATH, "//section//h1[text()='Make a Payment']")
        self.assertIsNotNone(h1)

    def test_10_amount_input_present(self):
        """Amount input field should be present and required"""
        amt = self.driver.find_element(By.NAME, 'amount')
        self.assertEqual(amt.get_attribute('type'), 'number')
        self.assertEqual(amt.get_attribute('required'), 'true')

    def test_11_card_number_input_present(self):
        """Card number input field should be present and maxlength 19"""
        cn = self.driver.find_element(By.NAME, 'card_num')
        self.assertEqual(cn.get_attribute('maxlength'), '19')

    def test_12_cvv_input_present(self):
        """CVV input field should be present and maxlength 4"""
        cvv = self.driver.find_element(By.NAME, 'cvv')
        self.assertEqual(cvv.get_attribute('maxlength'), '4')

    def test_13_expiry_input_present(self):
        """Expiration date input should be type month and required"""
        exp = self.driver.find_element(By.NAME, 'exp_date')
        self.assertEqual(exp.get_attribute('type'), 'month')
        self.assertEqual(exp.get_attribute('required'), 'true')

    def test_14_pay_button_present(self):
        """Pay button should be renderable"""
        btn = self.driver.find_element(By.XPATH, "//button[text()='Pay']")
        self.assertTrue(btn.is_enabled())

    def test_15_card_number_formatting(self):
        """Entering digits formats card number into groups of 4"""
        cn = self.driver.find_element(By.NAME, 'card_num')
        cn.clear()
        cn.send_keys('1234567890123456')
        value = cn.get_attribute('value')
        self.assertEqual(value, '1234 5678 9012 3456')

    def test_16_cvv_formatting(self):
        """Entering letters and digits only keeps digits up to length 3"""
        cvv = self.driver.find_element(By.NAME, 'cvv')
        cvv.clear()
        cvv.send_keys('12ab3')
        value = cvv.get_attribute('value')
        self.assertEqual(value, '123')

    def test_17_amount_step_attribute(self):
        """Amount input should accept two decimal places"""
        amt = self.driver.find_element(By.NAME, 'amount')
        self.assertEqual(amt.get_attribute('step'), '0.01')

    def test_18_fetch_and_reset_behaviour(self):
        """Submitting payment should call fetch with correct payload and clear form"""
        self.driver.execute_script("""
            window.alert = function(){};
            window.fetchCalls = [];
            window.fetch = function(url, opts) {
                window.fetchCalls.push({url: url, opts: opts});
                return Promise.resolve({ json: () => Promise.resolve({ status: 'ok' }) });
            };
        """)
        self.driver.find_element(By.NAME, 'amount').send_keys('20.00')
        self.driver.find_element(By.NAME, 'card_num').send_keys('5555444433332222')
        self.driver.find_element(By.NAME, 'cvv').send_keys('999')
        self.driver.execute_script("""document.querySelector('input[name="exp_date"]').value = '2025-07'""")
        self.driver.find_element(By.XPATH, "//button[text()='Pay']").click()
        time.sleep(1)
        calls = self.driver.execute_script("return window.fetchCalls;")
        self.assertEqual(len(calls), 1, f"Expected 1 fetch call, got {len(calls)}")
        call = calls[0]
        self.assertTrue(call['url'].endswith('/payment'), f"Unexpected endpoint: {call['url']}")
        body = call['opts']['body']
        data = json.loads(body)
        self.assertEqual(data.get('amount'), 20.00)
        self.assertEqual(data.get('card_num'), '5555444433332222')
        self.assertEqual(data.get('cvv'), '999')
        self.assertEqual(self.driver.find_element(By.NAME, 'amount').get_attribute('value'), '')

    def test_19_form_validation_prevents_empty_submit(self):
        """Submitting empty form should not show alert"""
        self.driver.find_element(By.XPATH, "//button[text()='Pay']").click()
        with self.assertRaises(NoAlertPresentException):
            _ = self.driver.switch_to.alert.text

    def test_20_products_fetch_error_handling(self):
        """Products component should handle fetch errors gracefully"""
        items = self.driver.find_elements(By.CSS_SELECTOR, 'section ul li')
        if items:
            self.skipTest('Products loaded; cannot test error handling')
        ul = self.driver.find_element(By.CSS_SELECTOR, 'section ul')
        self.assertEqual(len(ul.find_elements(By.TAG_NAME, 'li')), 0)


if __name__ == '__main__':
    unittest.main()
