Mockcat 

I have built one tool for android development workflow to help out in development and testing. The name of the tools is Mockcat. A very simple description of this tool is  Mockcat is an on-device proxy for API calls happening in mobile apps. Mockcat can serve a saved static response or redirect the request to the different API. 

I have already implemented this in our android app. I will give you all the implementation details from the current implementation. I want to extend this idea and create a kotlin multiplatform library which will be used in both android & iOS apps. I also have an idea to create a live server to serve all these responses & not just static response or redirection proxy. This live server idea is for future implementation. Currently I am only targeting the Android & iOS apps. 

How mockcat works  
Here is a high level overview of how mockcat works in the current implementation. Mainly, Mockcat has two components 

1. Mockcat database, which is accessed via a mockcat. Implemented using room db.
2. Mockcat interceptor, which is an implementation of the Okhttp interceptor.

The mockcat database contains the entries of API endpoints, some selected headers and their responses along with some other metadata. Whenever an OkHttp client is being created in all the debug builds, the mockcat interceptor will be added in the interceptor. Whenever an app makes any API call, it first goes through the mockcat interceptor, it checks if the URL is already available in the mockcat database, if not it will proceed forward. If the interceptor finds an entry in the mockcat database, based on the metadata and other things present in the database, mockcat will either serve the saved response or redirect the request to somewhere else.  

In the current android app, I have created some add on tools as well which will make developers lives a bit better. Listing them below. 

1. Gradle Integration  I have also created a gradle task which has one hardcoded file directory as an input. That directory contains the mocks for particular URLs. So, when a developer executes that gradle task, it takes all the files from that directory and creates entries in the mockcat database.
2. App Shortcuts integration  Currently mockcat lives as one the features in the developer configurations present in the android app. These developer configurations are accessible via the app shortcuts as well. So, one of the ways to open mockcat in the current app is using app-shotcuts.
3. Notification nudge  Whenever mockcat intercepts any request, mockcat intercepts the request and notification is fired to inform the user that this request has been preceded by mockcat & not the real server.
4. Import & Export from file  I have also added support to export all the entries of mockcat to a file and import any file which is exported from mockcat or follow the similar schema of mockcat export.
5. Multiple ADB devices support  In the branch in the android repository I had added the support to handle the multiple ADB devices. Branch name   enhancement/mockcatupdates

Here are implementation details from the current implementation:  
Here is the main package  /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat  
Now I will give you a package wise division with a one line description

1. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/ui → This is the compose UI for the mockcat user interface. Users can do CRUD operations on all mockcat entries. There are also utilities added to enable/disable mocks, import/export mocks and other user operations.
2. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/viewmodel → This is the main viewModel handling the UI inercations.
3. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/interceptor → This is the interceptor having implementation for the Interceptor.
4. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/data → This is database layer handling.
5. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/notification → This is the notification part, which informs user that mockcat has intercepted the request.
6. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/feature-abconfig/src/main/java/com/bms/abconfig/mockcat/cli → This is the place where we are handling incoming from gradle task.
7. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/mobile/app/build.gradle.kts → You can see the gradle task defined here which is executing the gradle task.
8. /Users/suraj.bichkunde/StudioProjects/bms-in-android-rename/buildSrc/src/main/kotlin/tasks/[MockcatImportTask.kt](http://MockcatImportTask.kt) → this is the actual gradle task which is being executed.
Here are my thoughts on the new implementation:  
These are some of the primary goals that I am looking forward  

1. An independent library which will be distributed through artifactory like maven
2. The core idea is very simple  HTTP proxy. This should be extensible. Multiple platforms, Multiple HTTP clients.
3. Easy integration in existing projects.

Inspirations : 

1. Chucker  [https://github.com/ChuckerTeam/chucker](https://github.com/ChuckerTeam/chucker)
2. Ktor  [https://github.com/ktorio/ktor](https://github.com/ktorio/ktor)

Why these inspirations ? 

- I really like how easy it is to integrate chucker. It has a very simple public API. In some ways, I see mockcat as a logical next step to chucker. What chucker does is just intercept the request and log it. We can see the logs, export the logs and Chucker is only for OkHttp. What mockcat is doing is intercepting the request and based on metadata responding back. Mockcat works for multiple clients and platforms. The library and library-no-op distribution is also very nice.   
- Ktor  I really like how Ktor is structured. Ktor is very minimalistic. It is very open and clean. I also like how Ktor is structured and served. You can see ktor-http, ktor-client, ktor-io, ktor-network, ktor-shared, etc. Ktor supports multiplatform. Ktor is very well written.

What I am looking for in new implementation:  
I want to divide the functionality in the separate modules. Here is some of my very initial thoughts  

1. mockcat-core – This will have the core mockcat functionality. This will be a compose multiplatform library. This will have a room db, user interface. This will be platform independent. This will expose a function which takes a plain HTTP request metadata as input, query the database and return the matching response. This will also expose some other utilities to interact with the room database.
2. mockcat-intercept-okhttp  This will also be an independent library which will use mockcat-core as a dependency. So, this will be targeted for the clients who are on android and using Okhttp client.
3. mockcat-intercept-ktor   This will also be an independent library which will use mockcat-core as a dependency. So, this will be targeted for the clients who are on android and using ktor client.
4. mockcat-intercept-fetch   Maybe this will be for the web developers. This will also be an independent library which will use mockcat-core as a dependency. So, this will be targeted for the clients who are on the web and using fetch API for making API calls.
5. mockcat-inercept-URLSession → I am not very sure on how this will work ? but we can have this library for the users of the iOS.

Along with this, we will have to build sample apps which will show how to use these libraries. I think this one is like a standard way of doing things. Same as ktor. 

Also, we will have to build the utilities, just like I have created in the current app. Like the gradle task, we can create a gradle plugin which can help users control the mockcat database from terminal. We can extend the same plugin to experiment with the live server as well. 

Let’s start: 

- We are building this from scratch.   
- Take a look at all the existing implementations. Check all the relevant code, even if I have not mentioned it explicitly.    
- Create a new git repository. Add ktlint. Add detect. Before committing anything make sure ktlint and detekt checks are passed.   
- For initial implementation let’s target only android and iOS. In android we are targeting two clients Okhttp & ktor. And for iOS we are targeting URLSession.  
- Once you get all the context. Requesting understanding and idea of what we are targeting, go for git and initial setup.   
- Then we will plan the whole task.   
- In the planning phase use all the best practices of jetpack compose, android and kotlin multiplatform.   
- You can use android cli for the best practices. If you run android help on the terminal you will see the instructions on how to use android cli. You can use this tool to get everything related to android. Doc search is particularly helpful when doing android related things.  
- In the planning phase what we need to do is, build the overall architecture for mockcat. Also, we will include all the subtasks and use subagents to do these tasks.   
- There is a lot of scope for parallelization in this task. For example, we can do the building of sample apps independently. Also, once the core is complete we can do the development for intercept-okhttp and intercept-ktor parallelly.   
- Also, we will not start working on the intercept or dependent library till we have the core ready.   
- We should agree on the public api of the library before we start building that library.   
- Follow all the  standard practices for kotlin and kotlin multiplatform.   
- In the planning phase itself, create the checkpoints and milestones. Commit code changes with a simple and clear commit message. Don’t overexplain things in the commit message.   
- Parallel agents can work on different branches.   
- I think what we are building should be very simple. So, we will not be using any DI framework. Because all of our libraries are simple, so we should not bloat them with a buch of dependencies. We will include only those dependencies which are required like room.   
- Based on your best understanding, create [Agnet.md](http://Agnet.md) file and agent skills and cursor rules as you see fit. Just make sure the rules and agent files are crisp so that they don’t unnecessarily burn the context & tokens.   
- Let’t hit and create the plan.