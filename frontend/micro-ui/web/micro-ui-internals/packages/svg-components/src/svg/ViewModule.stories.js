import React from "react";
import { ViewModule } from "./ViewModule";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewModule",
  component: ViewModule,
};

export const Default = () => <ViewModule />;
export const Fill = () => <ViewModule fill="blue" />;
export const Size = () => <ViewModule height="50" width="50" />;
export const CustomStyle = () => <ViewModule style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewModule className="custom-class" />;
export const Clickable = () => <ViewModule onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewModule {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
