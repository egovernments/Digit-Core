import React from "react";
import { TableView } from "./TableView";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TableView",
  component: TableView,
};

export const Default = () => <TableView />;
export const Fill = () => <TableView fill="blue" />;
export const Size = () => <TableView height="50" width="50" />;
export const CustomStyle = () => <TableView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TableView className="custom-class" />;
export const Clickable = () => <TableView onClick={()=>console.log("clicked")} />;

const Template = (args) => <TableView {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
