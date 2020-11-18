// BankImpl.java
//
// implementation of the Bank
//
import java.io.*;
import java.util.*;

public class BankImpl implements Bank {
private int n; // the number of threads in the system
private int m; // the number of resources

private int[] available; // the amount available of each resource
private int[][] maximum; // the maximum demand of each thread
private int[][] allocation; // the amount currently allocated to each thread
private int[][] need; // the remaining needs of each thread
private boolean[] released; // released customers

private void showAllMatrices(int[][] alloc, int[][] max, int[][] need, String msg) {
System.out.print("\tALLOCATION\t\tMAXIMUM\t\t\tNEED\n");
for (int i = 0; i < n; ++i){
System.out.print("\t");
if (released[i]){
System.out.print("-------\t\t--------\t\t--------\n");
}
else{
showVector(alloc[i],"\t\t");
showVector(max[i],"\t\t");
showVector(need[i],"\n");
}
}
}

private void showMatrix(int[][] matrix, String title, String rowTitle) {
System.out.println(title);
for (int i = 0; i < n; ++i){
showVector(matrix[i], "");
}
}

private void showVector(int[] vect, String msg) {
System.out.print("[");
for (int i = 0; i < m; ++i){
System.out.print(Integer.toString(vect[i]) + ' ');
}
System.out.print("]" + msg);
}

public BankImpl(int[] resources) { // create a new bank (with resources)
m = resources.length;
n = Customer.COUNT;
available = new int[m];
System.arraycopy(resources, 0, available, 0, m);
maximum = new int[n][m];
allocation = new int[n][m];
need = new int[n][m];
released = new boolean[n];
Arrays.fill(released, true);
}

public void updateCustomerCount(int _n){
n = _n;
}

// invoked by a thread when it enters the system; also records max demand
public void addCustomer(int threadNum, int[] allocated, int[] maxDemand) {
for (int i = 0; i < m; ++i){
allocation[threadNum][i] = allocated[i];
maximum[threadNum][i] = maxDemand[i];
need[threadNum][i] = maxDemand[i] - allocated[i];
}
released[threadNum] = false;
}

public void getState() { // output state for each thread
showAllMatrices(allocation, maximum, need, "");
}

private boolean isSafeState(int threadNum, int[] request) {
int[] currentAvailable = new int[m];
System.arraycopy(available,0, currentAvailable, 0, m);
int[][] currentAlloc = new int[n][m];
int[][] currentNeed = new int[n][m];
for (int i = 0; i < n; ++i){
System.arraycopy(allocation[i], 0, currentAlloc[i], 0, m);
System.arraycopy(need[i], 0, currentNeed[i], 0, m);
}

boolean[] finish = new boolean[n];
Arrays.fill(finish, false);

// pretend we grant the request to customer threadNum
for (int i = 0; i < m; ++i){
currentAvailable[i] -= request[i];
currentAlloc[threadNum][i] += request[i];
currentNeed[threadNum][i] -= request[i];
}

while (true) {
int index = -1;
for (int i = 0; i < n; ++i) {
boolean hasEnoughResource = true;
for (int j = 0; j < m; ++j) {
if (currentNeed[i][j] > currentAvailable[j]) {
hasEnoughResource = false;
break;
}
}
if (!finish[i] && hasEnoughResource) {
index = i;
break;
}
}

if (index > -1){
for (int i = 0; i < m; ++i){
currentAvailable[i] += currentAlloc[index][i];
finish[index] = true;
}
}
else break;
}

for (int i = 0; i < n; ++i){
if (!finish[i]) return false;
}
return true;
}

// make request for resources. will block until request is satisfied safely
public synchronized boolean requestResources(int threadNum, int[] request) {
// check for valid request
for (int i = 0; i < m; ++i){
if (request[i] > need[threadNum][i]){
request[i] = need[threadNum][i];
}
}
System.out.print("#P"+ threadNum + " RQ:");
showVector(request, ", needs: ");
showVector(need[threadNum], ", available:");
showVector(available, "\n");

// check for available resource
for (int i = 0; i < m; ++i){
if (request[i] > available[i]){
System.out.println("--->DENIED");
return false;
}
}

if (isSafeState(threadNum, request)){
System.out.print("---> APPROVED, #P" + threadNum + " now at: ");
// give customer threadNum the resource
for (int i = 0; i < m; ++i){
available[i] -= request[i];
allocation[threadNum][i] += request[i];
need[threadNum][i] -= request[i];
}

showVector(allocation[threadNum], " available");
showVector(available, "\n");
showAllMatrices(allocation, maximum, need, "\n");

for (int i = 0; i < m; ++i){
if (need[threadNum][i] != 0){
return false; // customer isn't finished yet
}
}
return true; // customer finished, waiting to be released
}
System.out.println("--->DENIED"); // request doesn't lead to a safe state
return false;
}

public synchronized void releaseResources(int threadNum, int[] release) {
System.out.print("========> #P" + threadNum + " has all its resources!" +
"RELEASING ALL and SHUTTING DOWN...\n");
System.out.print("-----------#P" + threadNum + " releasing: ");
showVector(allocation[threadNum], ", allocated=");
for (int i = 0; i < m; ++i){
available[i] += allocation[threadNum][i];
allocation[threadNum][i] = 0;
}
showVector(allocation[threadNum], "\n");
released[threadNum] = true;
}
}
