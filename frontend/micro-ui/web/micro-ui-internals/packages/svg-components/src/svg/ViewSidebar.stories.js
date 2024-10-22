import React from "react";
import { ViewSidebar } from "./ViewSidebar";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewSidebar",
  component: ViewSidebar,
};

export const Default = () => <ViewSidebar />;
export const Fill = () => <ViewSidebar fill="blue" />;
export const Size = () => <ViewSidebar height="50" width="50" />;
export const CustomStyle = () => <ViewSidebar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewSidebar className="custom-class" />;
export const Clickable = () => <ViewSidebar onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewSidebar {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
