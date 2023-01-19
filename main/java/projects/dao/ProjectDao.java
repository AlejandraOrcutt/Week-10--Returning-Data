package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

/** This class used JDBC to perform CRUD operations on the project tables.
 * 
 * @author AO Promineo
 *
 */

@SuppressWarnings("unused")

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		/* now we will do a Try to get the connection */
		try (Connection connection = DbConnection.getConnection()) {
			startTransaction(connection);
			
			try {
				Project project  = null;
				
				try (PreparedStatement stmt = connection.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs =stmt.executeQuery()){
						if(rs.next()) {
							project = extract(rs, Project.class);
							
						}
					}
				}
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(connection, projectId));
					project.getSteps().addAll(fetchStepsForProject(connection, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(connection, projectId));
				}
				
				commitTransaction(connection);
				return Optional.ofNullable(project);
			
		} 
			catch (Exception e) {
			rollbackTransaction(connection);
			throw new DbException (e);
			}
		}
			catch (SQLException e) {
				throw new DbException(e);
			}
		}
	
	/** insert a project row into the project table.
	 * 
	 * @param project The project object to insert
	 * @return The project object with the primary key
	 * @throws DbException Thrown if an error occurs inserting the row
	 */

	public Project insertProject(Project project) {
		// @formatter:off
	String sql = ""
			+ "INSERT INTO "+ PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
			+ "VALUES"
			+"(? ,? ,? ,? ,?)";	
		// @formatter:on

		try (Connection connection = DbConnection.getConnection()) {
			startTransaction(connection);

			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();
				Integer projectId = getLastInsertId(connection, PROJECT_TABLE);

				commitTransaction(connection);

				project.setProjectId(projectId);
				return project;
			} catch (Exception e) {
				rollbackTransaction(connection);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " +PROJECT_TABLE + " ORDER BY project_name";
		
		try ( Connection connection = DbConnection.getConnection()){
			
			startTransaction(connection);
			
			try (PreparedStatement stmt = connection.prepareStatement(sql)){
			try (ResultSet rs = stmt.executeQuery()){
				List<Project> projects = new LinkedList<>();
				
				while(rs.next()) {
					projects.add(extract(rs, Project.class));
				}
				
				return projects;
			}
				
		} 
			catch (Exception e) {
			rollbackTransaction(connection);
			throw new DbException(e);
		}
		}catch (SQLException e) {
			throw new DbException(e);
				
		}
	}
	
	private List<Category> fetchCategoriesForProject(Connection connection, Integer projectId) throws SQLException{
		// @formatter:off
		String sql = ""
				+ "SELECT  c. * FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id =?";
	// @formatter:on
				
				try (PreparedStatement stmt = connection.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						List<Category> categories = new LinkedList<>();
						
						while(rs.next()) {
							categories.add(extract(rs, Category.class));
							}
						return categories;
						}
					}
					
				}
	private List<Step> fetchStepsForProject(Connection connection, Integer projectId) throws SQLException{
		
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

				
				try (PreparedStatement stmt = connection.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						List<Step> steps = new LinkedList<>();
						
						while(rs.next()) {
							steps.add(extract(rs, Step.class));
							}
						return steps;
						}
					}
					
				}
private List<Material> fetchMaterialsForProject(Connection connection, Integer projectId) throws SQLException{
		
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id =?";

				
				try (PreparedStatement stmt = connection.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						List<Material> materials = new LinkedList<>();
						
						while(rs.next()) {
							materials.add(extract(rs, Material.class));
							}
						return materials;
						}
					}
					
				}

public boolean modifyProjectDetails(Project project) {
	// @formater:off
	String sql = ""
			+ "UPDATE " + PROJECT_TABLE + " SET "
			+ "project_name = ?, "
			+ "estimated_hours = ?, "
			+ "actual_hours = ?, "
			+ "difficulty = ?, "
			+ "notes = ? "
			+ "WHERE project_id = ?";
	// @formatter:on
	
	try (Connection connection = DbConnection.getConnection()) {
		startTransaction(connection);
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			setParameter(stmt, 1, project.getProjectName(), String.class);
			setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
			setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
			setParameter(stmt, 4, project.getDifficulty(), Integer.class);
			setParameter(stmt, 5, project.getNotes(), String.class);
			setParameter(stmt, 6, project.getProjectId(), Integer.class);
	
		boolean modified = stmt.executeUpdate() == 1;
		commitTransaction(connection);
		return modified;
	    }
		catch (Exception e) {
			rollbackTransaction(connection);
			throw new DbException(e);
		}
	}
	catch(Exception e) {
		throw new DbException(e);
	}
	
}
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try (Connection connection = DbConnection.getConnection()) {
			startTransaction(connection);
			
			try (PreparedStatement stmt = connection.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(connection);
				return deleted;
			    }
			catch (Exception e) {
				rollbackTransaction(connection);
				throw new DbException(e);
			   }
			}
		catch (SQLException e) {
			throw new DbException(e);
			}
		}

	
}			
	

