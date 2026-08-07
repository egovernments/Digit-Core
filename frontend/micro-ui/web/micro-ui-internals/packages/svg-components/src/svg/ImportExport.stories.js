import React from "react";
import { ImportExport } from "./ImportExport";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ImportExport",
  component: ImportExport,
};

export const Default = () => <ImportExport />;
export const Fill = () => <ImportExport fill="blue" />;
export const Size = () => <ImportExport height="50" width="50" />;
export const CustomStyle = () => <ImportExport style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ImportExport className="custom-class" />;

export const Clickable = () => <ImportExport onClick={()=>console.log("clicked")} />;

const Template = (args) => <ImportExport {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
