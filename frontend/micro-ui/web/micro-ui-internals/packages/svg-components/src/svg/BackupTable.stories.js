import React from "react";
import { BackupTable } from "./BackupTable";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BackupTable",
  component: BackupTable,
};

export const Default = () => <BackupTable />;
export const Fill = () => <BackupTable fill="blue" />;
export const Size = () => <BackupTable height="50" width="50" />;
export const CustomStyle = () => <BackupTable style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BackupTable className="custom-class" />;

export const Clickable = () => <BackupTable onClick={()=>console.log("clicked")} />;

const Template = (args) => <BackupTable {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
