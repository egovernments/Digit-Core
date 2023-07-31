import React from "react";
import { ViewAgenda } from "./ViewAgenda";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewAgenda",
  component: ViewAgenda,
};

export const Default = () => <ViewAgenda />;
export const Fill = () => <ViewAgenda fill="blue" />;
export const Size = () => <ViewAgenda height="50" width="50" />;
export const CustomStyle = () => <ViewAgenda style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewAgenda className="custom-class" />;
export const Clickable = () => <ViewAgenda onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewAgenda {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
