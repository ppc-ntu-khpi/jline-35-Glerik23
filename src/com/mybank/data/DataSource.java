package com.mybank.data;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

public class DataSource {

    private String filePath;

    public DataSource(String dataFilePath) {
        this.filePath = dataFilePath;
    }

    public void loadData() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Customer currentCustomer = null;

            line = reader.readLine();
            if (line == null) {
                throw new IOException("test.dat пуст или имеет неверный формат: отсутствует количество клиентов.");
            }
            int totalCustomers = Integer.parseInt(line.trim());

            reader.readLine();

            for (int i = 0; i < totalCustomers; i++) {
                line = reader.readLine();
                if (line == null || line.trim().isEmpty()) {

                    line = reader.readLine();
                    if (line == null || line.trim().isEmpty()) {
                        throw new IOException("Неожиданный конец файла или слишком много пустых строк перед данными клиента для клиента " + i);
                    }
                }

                String[] customerParts = line.trim().split("\t");
                if (customerParts.length < 3) {
                    throw new IOException("Строка клиента имеет неверный формат: " + line);
                }
                String firstName = customerParts[0];
                String lastName = customerParts[1];
                int numOfAccounts = Integer.parseInt(customerParts[2]);

                Bank.addCustomer(firstName, lastName);
                currentCustomer = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);

                for (int j = 0; j < numOfAccounts; j++) {
                    line = reader.readLine();
                    if (line == null || line.trim().isEmpty()) {
                        throw new IOException("Отсутствуют данные счета для клиента " + firstName + " " + lastName + " (ожидалось " + numOfAccounts + " счетов, получено " + j + ")");
                    }

                    String[] accountParts = line.trim().split("\t");
                    if (accountParts.length < 3) {
                        throw new IOException("Строка счета имеет неверный формат: " + line);
                    }

                    char accountType = accountParts[0].charAt(0);
                    float initBalance = Float.parseFloat(accountParts[1]);
                    float param = Float.parseFloat(accountParts[2]);

                    switch (accountType) {
                        case 'S':
                            currentCustomer.addAccount(new SavingsAccount(initBalance, param));
                            break;
                        case 'C':
                            currentCustomer.addAccount(new CheckingAccount(initBalance, param));
                            break;
                        default:
                            System.err.println("Неизвестный тип счета: " + accountType + " в строке: " + line);
                    }
                }

                if (i < totalCustomers - 1) {
                    line = reader.readLine();
                    if (line == null || !line.trim().isEmpty()) {
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга числа в test.dat. Проверьте формат данных: " + e.getMessage());
            throw new IOException("Ошибка формата данных в test.dat: " + e.getMessage(), e);
        } catch (InputMismatchException e) {
            System.err.println("Ошибка ввода данных в test.dat. Проверьте формат данных: " + e.getMessage());
            throw new IOException("Ошибка ввода данных в test.dat: " + e.getMessage(), e);
        } catch (NoSuchElementException e) {
            System.err.println("Неожиданный конец файла или неполные данные в test.dat: " + e.getMessage());
            throw new IOException("Неполные данные в test.dat: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            System.err.println("Внутренняя ошибка при загрузке данных (возможно, NullPointerException). Проверьте структуру Bank/Customer/Account классов: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Внутренняя ошибка загрузки данных из test.dat.", e);
        }
    }
}