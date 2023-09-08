import React from "react";
import { ViewInAr } from "./ViewInAr";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewInAr",
  component: ViewInAr,
};

export const Default = () => <ViewInAr />;
export const Fill = () => <ViewInAr fill="blue" />;
export const Size = () => <ViewInAr height="50" width="50" />;
export const CustomStyle = () => <ViewInAr style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewInAr className="custom-class" />;
export const Clickable = () => <ViewInAr onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewInAr {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
