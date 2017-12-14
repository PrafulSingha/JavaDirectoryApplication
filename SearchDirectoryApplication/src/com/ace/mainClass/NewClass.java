package com.ace.mainClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public class NewClass {
	   private static Map<WatchKey, Path> keyPathMap = new HashMap<>();

	   public static void main (String[] args) throws Exception {
	       try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
	           registerDir(Paths.get("C:\\New folder"), watchService);
	           startListening(watchService);
	       }
	   }

	   private static void registerDir (Path path, WatchService watchService) throws
	                       IOException {


	       if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
	           return;
	       }

	       System.out.println("registering: " + path);


	       WatchKey key = path.register(watchService,
	                           StandardWatchEventKinds.ENTRY_CREATE,
	                           StandardWatchEventKinds.ENTRY_MODIFY,
	                           StandardWatchEventKinds.ENTRY_DELETE);
	       keyPathMap.put(key, path);


	       for (File f : path.toFile().listFiles()) {
	           registerDir(f.toPath(), watchService);
	           if(FilenameUtils.isExtension(f.getName(),"txt") && FilenameUtils.isExtension(f.getName(),"csv")){
	        	   List<String> list = new ArrayList<>();

	       		try (Stream<String> stream = Files.lines(Paths.get(f.getName()))) {

	       			//1. filter line 3
	       			//2. convert all content to upper case
	       			//3. convert it into a List
	       			list = stream
	       					.filter(line -> !line.startsWith("line3"))
	       					.map(String::toUpperCase)
	       					.collect(Collectors.toList());

	       		} catch (IOException e) {
	       			e.printStackTrace();
	       		}
	           }
	       }
	   }

	   private static void startListening (WatchService watchService) throws Exception {
	       while (true) {
	           WatchKey queuedKey = watchService.take();
	           for (WatchEvent<?> watchEvent : queuedKey.pollEvents()) {
	               System.out.printf("Event... kind=%s, count=%d, context=%s Context type=%s%n",
	                                   watchEvent.kind(),
	                                   watchEvent.count(), watchEvent.context(),
	                                   ((Path) watchEvent.context()).getClass());

	               //do something useful here

	               if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
	                   //this is not a complete path
	                   Path path = (Path) watchEvent.context();
	                   //need to get parent path
	                   Path parentPath = keyPathMap.get(queuedKey);
	                   //get complete path
	                   path = parentPath.resolve(path);

	                   registerDir(path, watchService);
	               }
	           }
	           if(!queuedKey.reset()){
	               keyPathMap.remove(queuedKey);
	           }
	           if(keyPathMap.isEmpty()){
	               break;
	           }
	       }
	   }}
