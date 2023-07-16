import React from "react";
import { HomeRepairService } from "./HomeRepairService";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HomeRepairService",
  component: HomeRepairService,
};

export const Default = () => <HomeRepairService />;
export const Fill = () => <HomeRepairService fill="blue" />;
export const Size = () => <HomeRepairService height="50" width="50" />;
export const CustomStyle = () => <HomeRepairService style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HomeRepairService className="custom-class" />;
