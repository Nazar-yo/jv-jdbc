package mate.jdbc.dao;

import mate.jdbc.lib.Dao;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        String insertRequest = "insert into manufacturers(name, country) values (?, ?);";
        try(Connection connection = ConnectionUtil.getConnection();
            PreparedStatement createManufacturerStatement
                    = connection.prepareStatement(insertRequest, Statement.RETURN_GENERATED_KEYS)) {
            createManufacturerStatement.setString(1, manufacturer.getName());
            createManufacturerStatement.setString(2, manufacturer.getCountry());
            createManufacturerStatement.executeUpdate();
            ResultSet generatedKeys = createManufacturerStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                manufacturer.setId(id);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Can`t insert values into manufacturers DB", e);
        }
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        Optional<Manufacturer> manufacturerOptional = Optional.empty();
        String getByIdRequest = "select * from manufacturers where is_deleted = false and id = " + id;
        try(Connection connection = ConnectionUtil.getConnection();
            Statement getByIdStatement = connection.createStatement()) {
            ResultSet resultSet = getByIdStatement.executeQuery(getByIdRequest);
            if (resultSet.next()) {
                manufacturerOptional = Optional.of(setManufacturerFromDb(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Can`t get manufacturer by id " + id + " from DB", e);
        }
        return manufacturerOptional;
    }

    @Override
    public List<Manufacturer> getAll() {
        List<Manufacturer> allManufacturer = new ArrayList<>();
        String selectAllRequest = "SELECT * FROM manufacturers where is_deleted = FALSE;";
        try(Connection connection = ConnectionUtil.getConnection();
            Statement getAllManufacturerStatement = connection.createStatement()) {
            ResultSet resultSet = getAllManufacturerStatement.executeQuery(selectAllRequest);
            while (resultSet.next()) {
                allManufacturer.add(setManufacturerFromDb(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Can`t get all manufacturers from DB", e);
        }
        return allManufacturer;
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        String updateManufacturerRequest = "update manufacturers set name = ?, country = ? " +
                "where id = ? and is_deleted = FALSE;";
        try (Connection connection = ConnectionUtil.getConnection();
            PreparedStatement updateManufacturerStatement = connection.prepareStatement(updateManufacturerRequest)) {
            updateManufacturerStatement.setString(1, manufacturer.getName());
            updateManufacturerStatement.setString(2, manufacturer.getCountry());
            updateManufacturerStatement.setLong(3, manufacturer.getId());
            if (updateManufacturerStatement.executeUpdate() >= 1) {
                return manufacturer;
            }
            throw new RuntimeException("No such manufacture");
        } catch (SQLException e) {
            throw new RuntimeException("Can`t update manufacturer by id " + manufacturer.getId() + "from DB", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteRequest = "update manufacturers set is_deleted = true where id = ?";
        try(Connection connection = ConnectionUtil.getConnection();
            PreparedStatement deleteStatement = connection.prepareStatement(deleteRequest)) {
            deleteStatement.setLong(1, id);
            return deleteStatement.executeUpdate() >= 1;
        } catch (SQLException e) {
            throw new RuntimeException("Can`t delete manufacturer by id " + id + "from DB", e);
        }
    }

    private Manufacturer setManufacturerFromDb(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        String country = resultSet.getString("country");
        Long id = resultSet.getObject("id", Long.class);
        Manufacturer manufacturer = new Manufacturer(name, country);
        manufacturer.setId(id);
        return manufacturer;
    }
}