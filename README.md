# swingtime

A simple project management with users, projects and tasks:

  * Projects have tasks
  * Tasks can get assigned to users

## Domain design

### Project entity
Project
  * name: String
  * tasks: Seq[Task]

Task
  * description: String
  * assignedTo: User

CRUD commands + Events
  * CreateProject => ProjectUpdated
  * GetProject => (return Project entity)
  * UpdateProject => ProjectUpdated
  * AddTask => TaskAdded
  * AssignTaskToUser => TaskAssignedToUser
  * DeleteProject => ProjectDeleted

### User entity
User
  * name: String

CRUD commands + Events
  * CreateUser => UserUpdated
  * GetUser => (return User entity)
  * UpdateUser => UserUpdated
  * DeleteUser => UserDeleted