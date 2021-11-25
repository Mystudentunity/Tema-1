package demo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository repository;

    public List<TaskModel> getTasks(String title, String description, String assignedTo, TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) {
        return repository.findAll().stream()
                .filter(task -> isMatch(task, title, description, assignedTo, status, severity))
                .collect(Collectors.toList());
    }
    private boolean isMatch (TaskModel task, String title, String description, String assignedTo, TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) {
        return (title == null || task.getTitle().toLowerCase().startsWith(title.toLowerCase()))
                && (description == null || task.getDescription().toLowerCase().startsWith(description.toLowerCase()))
                && (assignedTo == null || task.getAssignedTo().toLowerCase().startsWith(assignedTo.toLowerCase()))
                && (status == null || task.getStatus().equals(status))
                && (severity == null || task.getSeverity().equals(severity));
    }

    public Optional<TaskModel> getTask(String id) {
        return repository.findById(id);
    }

    public TaskModel addTask (TaskModel task) throws IOException {
        repository.save(task);
        return task;
    }

    public boolean updateTask (String id, TaskModel task) throws IOException {
        if(repository.findById(id).isPresent()) {
            task.setId(id);
            repository.save(task);
            return true;
        } else {
            return false;
        }
    }

    public boolean patchTask (String id, TaskModel task) throws IOException {
        Optional<TaskModel> existingTask = repository.findById(id);
        if(existingTask.isPresent()) {
            existingTask.get().patch(task);
            repository.save(existingTask.get());
            return true;
        } else {
            return false;
        }
    }
    public boolean deleteTask (String id) throws IOException {
        return repository.deleteById(id);
    }

    public void extractTasksCsv(Writer writer, String title, String description, String assignedTo,
                                TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) throws IOException
    {

        List<TaskModel> tasks = getTasks(title, description, assignedTo, status, severity);
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
        for(TaskModel task : tasks)
        {
            printer.printRecord(task.getId(), task.getAssignedTo(),
                    task.getDescription(), task.getSeverity(), task.getStatus(),
                    task.getTitle());
        }
    }

    public void extractTasksXml(Writer write, String title, String description, String assignedTo,
                                TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) throws IOException
    {
        Document document = DocumentHelper.createDocument();
        List<TaskModel> tasks = getTasks(title, description, assignedTo, status, severity);

        Element root = document.addElement("tasks");

        for(TaskModel task : tasks)
        {

            Element element = root.addElement("task").addAttribute("id", task.getId());
            element.addElement("title").addText(task.getTitle());
            element.addElement("description").addText(task.getDescription());
            element.addElement("assignedTo").addText(task.getAssignedTo());
            element.addElement("status").addText(task.getStatus().toString());
            element.addElement("severity").addText(task.getSeverity().toString());
        }
        document.write(write);
    }

//    public List<Object> getTasksFiltered(String title, String description, String assignedTo,
//                                               TaskModel.TaskStatus status, TaskModel.TaskSeverity severity, String fields) throws IOException
//    {
//        List<TaskModel> tasks = getTasks(title, description, assignedTo, status, severity);
//        if(tasks.isEmpty()) {
//            return null;
//        } else {
//            List<Object> items;
//            if(fields != null && !fields.isBlank()) {
//                items = tasks.stream().map(task -> task.sparseFields(fields.split(","))).collect(Collectors.toList());
//            } else {
//                items = new ArrayList<>(tasks);
//            }
//
//            return items;
//        }
//    }
}
