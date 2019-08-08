# Spring-Distributed-State-Machine
A Distributed and Persisting State Machine implementation using Spring State Machine Framework, Zookeeper 3.5.4 and Apache Curator 4 with mariaDB.

Installation<br/>
-As it is a Maven project, don't forget to install Maven.<br/>
-To setup zookeepers with curator look at : https://github.com/DogukanKundum/HostMngOnCurator<br/>
-Cd to zookeeper/bin and type ./zkServer.sh start (for Linux) for all zookeepers you want to start.(It needs at least 3 zookepers to work)<br/>
-Example application.properties for 3 apps

App1: https://user-images.githubusercontent.com/32279212/62688747-2108b400-b9d2-11e9-8296-b086b8e9a6dc.png  <br/>
App2: https://user-images.githubusercontent.com/32279212/62688932-7e046a00-b9d2-11e9-8c84-4de4b58495f7.png   <br/>
App3: https://user-images.githubusercontent.com/32279212/62688975-93799400-b9d2-11e9-9459-b2c30bd607f1.png <br/>
