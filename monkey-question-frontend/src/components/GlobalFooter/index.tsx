"use client";
import React from "react";
import "./index.css";

/**
 * 全局底部栏组件
 *
 * @author allen
 */
export default function GlobalFooter() {
    const currentYear = new Date().getFullYear();

    return (
        <div className="global-footer">
            <div>© {currentYear} 猿题刷题平台</div>
            <div>
                <a href="https://www.github.com/KiveAllen" target="_blank">
                    作者：程序员KiveAllen
                </a>
            </div>
        </div>
    );
}
