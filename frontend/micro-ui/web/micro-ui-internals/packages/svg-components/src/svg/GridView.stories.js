import React from "react";
import { GridView } from "./GridView";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "GridView",
  component: GridView,
};

export const Default = () => <GridView />;
export const Fill = () => <GridView fill="blue" />;
export const Size = () => <GridView height="50" width="50" />;
export const CustomStyle = () => <GridView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GridView className="custom-class" />;

export const Clickable = () => <GridView onClick={()=>console.log("clicked")} />;

const Template = (args) => <GridView {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
