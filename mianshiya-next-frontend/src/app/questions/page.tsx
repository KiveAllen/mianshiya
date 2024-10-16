import styles from "./page.module.css";
import {Button} from "antd";

export default function Home() {
    return (
        <div className={styles.page}>
            <main className={styles.main}>
                <Button type="primary">题目大全</Button>
            </main>
        </div>
    );
}
