package nnu.wyz.utils;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    private static final int INF = Integer.MAX_VALUE;
    private static final int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static int n, m;
    private static int[][] grid, dist;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        n = scanner.nextInt();
        m = scanner.nextInt();
        grid = new int[n][m];
        dist = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                grid[i][j] = scanner.nextInt();
                dist[i][j] = INF;
            }
        }
        dijkstra();
        System.out.println(dist[n - 1][m - 1]);
    }

    private static void dijkstra() {
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
        pq.offer(new int[]{0, 0, 0});
        dist[0][0] = 0;
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int x = cur[0], y = cur[1];
            if (x == n - 1 && y == m - 1) {
                return;
            }
            for (int[] dir : dirs) {
                int nx = x + dir[0], ny = y + dir[1];
                if (nx >= 0 && nx < n && ny >= 0 && ny < m) {
                    int cost = dist[x][y] + grid[nx][ny];
                    if (cost < dist[nx][ny]) {
                        dist[nx][ny] = cost;
                        pq.offer(new int[]{nx, ny, cost});
                    }
                }
            }
        }
    }
}
