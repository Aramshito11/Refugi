import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;


public class Refugi {
    private static Connection connection; // Conexión a la base de datos

    static void crearInterfaz() {
        JFrame frame = new JFrame("Sistema de Reservas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 400);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel reservaPanel = crearPestañaReserva();
        JPanel revisarPanel = creatPestañaRevisar();
        JPanel listaPanel = crearPestañaLista();

        tabbedPane.addTab("Reserva", reservaPanel);
        tabbedPane.addTab("Revisar", revisarPanel);
        tabbedPane.addTab("Lista", listaPanel);

        frame.getContentPane().add(tabbedPane);
        frame.setVisible(true);
    }
    private static JPanel crearPestañaReserva() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 2));

        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField();

        JLabel lblDNI = new JLabel("DNI:");
        JTextField txtDNI = new JTextField();

        JLabel lblFechaInicio = new JLabel("Fecha de inicio:");
        JTextField txtFechaInicio = new JTextField();

        JLabel lblFechaFinal = new JLabel("Fecha final:");
        JTextField txtFechaFinal = new JTextField();

        JLabel lblNumPersonas = new JLabel("Número de personas:");
        JTextField txtNumPersonas = new JTextField();

        JButton btnSeleccionarFoto = new JButton("Seleccionar foto");
        JButton btnEnviar = new JButton("Enviar");

        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblApellido);
        panel.add(txtApellido);
        panel.add(lblDNI);
        panel.add(txtDNI);
        panel.add(lblFechaInicio);
        panel.add(txtFechaInicio);
        panel.add(lblFechaFinal);
        panel.add(txtFechaFinal);
        panel.add(lblNumPersonas);
        panel.add(txtNumPersonas);
        panel.add(btnSeleccionarFoto);
        panel.add(btnEnviar);
        Label lblFoto = new Label();
        final String[] fotoSeleccionada = new String[1];
        // Agregar un ActionListener para el botón de selección de foto
        btnSeleccionarFoto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int seleccion = fileChooser.showOpenDialog(panel);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    // Obtener el archivo seleccionado
                    File archivo = fileChooser.getSelectedFile();
                    // Mostrar el nombre del archivo seleccionado en la etiqueta

                    lblFoto.setText(archivo.getName());
                    // Guardar la ruta del archivo en una variable
                    fotoSeleccionada[0] = archivo.getAbsolutePath();
                }
            }
        });

        // Agregar un ActionListener para el botón "Enviar"
        btnEnviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Obtener los valores ingresados por el usuario
                String nombre = txtNombre.getText();
                String apellido = txtApellido.getText();
                String dni = txtDNI.getText();
                String fechaInicio = txtFechaInicio.getText();
                String fechaFinal = txtFechaFinal.getText();
                int numPersonas = Integer.parseInt(txtNumPersonas.getText());

                // Guardar los datos en la base de datos
                guardarCliente(nombre, apellido, dni, fotoSeleccionada[0], fechaInicio, fechaFinal, numPersonas);
            }
        });

        return panel;
    }
    private static JPanel creatPestañaRevisar(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2));


        JLabel lblBlanco = new JLabel("");

        JLabel lblID = new JLabel("Introduzca su DNI:");
        JTextField txtID = new JTextField();
        JButton btnEnviar2 = new JButton("Enviar");

        panel.add(lblID);
        panel.add(txtID);
        panel.add(btnEnviar2);
        panel.add(lblBlanco);



        btnEnviar2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String dni = txtID.getText();
                buscarPorNombre(dni,panel);
            }
        });

        return panel;
    }

    private static JPanel crearPestañaLista() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Crear el modelo de tabla
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("DNI");
        tableModel.addColumn("Nombre");
        tableModel.addColumn("Apellido");
        tableModel.addColumn("Inicio");
        tableModel.addColumn("Final");
        tableModel.addColumn("Personas");

        // Crear la tabla con el modelo
        JTable table = new JTable(tableModel);

        // Crear el scroll pane para la tabla
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Obtener los datos de la tabla "reservas"
        obtenerReservas(tableModel);

        return panel;
    }

    private static void obtenerReservas(DefaultTableModel tableModel) {
        try {
            String query = "SELECT * FROM reservas";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Limpiar la tabla
            tableModel.setRowCount(0);

            // Agregar las filas de la tabla "reservas" al modelo de tabla
            while (resultSet.next()) {
                String nom = resultSet.getString("Nom");
                String cognom = resultSet.getString("Cognom");
                String dni = resultSet.getString("DNI");
                String dinici = resultSet.getString("DataInici");
                String dfinal = resultSet.getString("DataFinal");
                Integer persones = resultSet.getInt("NumPersones");

                Object[] fila = {dni, nom, cognom, dinici, dfinal, persones};
                tableModel.addRow(fila);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al obtener las reservas");
        }
    }




    private static void guardarCliente(String nombre, String apellido, String dni, String fotoSeleccionada, String fechaInicio, String fechaFinal, int numPersonas) {
        try {
            File file = new File(fotoSeleccionada);
            FileInputStream fis = new FileInputStream(file);
            byte[] fotoBytes = new byte[(int) file.length()];
            fis.read(fotoBytes);
            fis.close();
            // Preparar la consulta SQL
            String query = "INSERT INTO reservas (DNI, nom, cognom, DataInici, DataFinal, NumPersones, FotoDNI) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, dni);
            statement.setString(2, nombre);
            statement.setString(3, apellido);
            statement.setString(4, fechaInicio);
            statement.setString(5, fechaFinal);
            statement.setInt(6, numPersonas);
            statement.setBytes(7, fotoBytes);

            statement.executeUpdate();
            statement.close();
            JOptionPane.showMessageDialog(null, "Cliente guardado correctamente");
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el cliente");
        }
    }
    private static void buscarPorNombre(String dni, JPanel panel) {
        try {
            String sql = "SELECT * FROM reservas WHERE DNI = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dni);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nom = resultSet.getString("Nom");
                String cognom = resultSet.getString("Cognom");
                String cdni = resultSet.getString("DNI");
                String dinici = resultSet.getString("DataInici");
                String dfinal = resultSet.getString("DataFinal");
                Integer persones = resultSet.getInt("NumPersones");
                byte[] foto = resultSet.getBytes("FotoDNI");


                // Mostrar los resultados en el panel
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.anchor = GridBagConstraints.WEST;
                constraints.insets = new Insets(5, 5, 5, 5);

                JLabel lblNombre = new JLabel("Nombre:");
                JLabel lblApellido = new JLabel("Apellido:");
                JLabel lblDNI = new JLabel("DNI:");
                JLabel lblFechaInicio = new JLabel("Fecha de inicio:");
                JLabel lblFechaFinal = new JLabel("Fecha final:");
                JLabel lblNumPersonas = new JLabel("Número de personas:");

                JLabel lblNomValor = new JLabel(nom);
                JLabel lblCognomValor = new JLabel(cognom);
                JLabel lblDNIValor = new JLabel(cdni);
                JLabel lblIniciValor = new JLabel(dinici);
                JLabel lblFinalValor = new JLabel(dfinal);
                JLabel lblNumValor = new JLabel(Integer.toString(persones));
                constraints.gridx = 0;
                constraints.gridy = 1;
                panel.add(lblNombre, constraints);

                constraints.gridx = 1;
                panel.add(lblNomValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 2;
                panel.add(lblApellido, constraints);

                constraints.gridx = 1;
                panel.add(lblCognomValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 3;
                panel.add(lblDNI, constraints);

                constraints.gridx = 1;
                panel.add(lblDNIValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 4;
                panel.add(lblFechaInicio, constraints);

                constraints.gridx = 1;
                panel.add(lblIniciValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 5;
                panel.add(lblFechaFinal, constraints);

                constraints.gridx = 1;
                panel.add(lblFinalValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 6;
                panel.add(lblNumPersonas, constraints);

                constraints.gridx = 1;
                panel.add(lblNumValor, constraints);

                constraints.gridx = 0;
                constraints.gridy = 7;

                ImageIcon imageIcon = new ImageIcon(foto);
                Image image = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(image);
                JLabel lblImatge= new JLabel();
                lblImatge.setIcon(resizedIcon);
                lblImatge.setPreferredSize(new Dimension(300, 300));

                panel.add(lblImatge);

            } else {
                JOptionPane.showMessageDialog(null, "No se encontraron resultados para el nombre: " + dni, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al buscar datos en la base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Establecer la conexión a la base de datos (ejemplo utilizando MySQL)
    static void establecerConexion() {


            try (FileInputStream f = new FileInputStream("Projecte final\\files\\conexcióBDD.properties")) {

                Properties pros = new Properties();
                pros.load(f);

                String url = pros.getProperty("url");
                String user = pros.getProperty("user");
                String password = pros.getProperty("password");
                connection = DriverManager.getConnection(url, user, password);
            } catch (IOException | SQLException e) {
                System.out.println(e.getMessage());
            }
        }

}
